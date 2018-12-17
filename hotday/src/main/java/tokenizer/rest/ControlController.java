package tokenizer.rest;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tokenizer.DataSources;
import tokenizer.Stability;
import tokenizer.events.Deployment;
import tokenizer.util.Network;
import tokenizer.util.ShutdownThread;

@RestController("control-service")
public class ControlController {
	
	private final DataSources dataSources;
	
	public ControlController(ApplicationContext ctx) {
		Objects.requireNonNull(ctx);
		
		dataSources = ctx.getBean(DataSources.class);
	}
	
	@RequestMapping("/ip")
	public ResponseEntity<String> ip() throws IOException {
		return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(Network.localIP());
	}
	
	@RequestMapping("/shutdown")
	public ResponseEntity<String> shutdown() throws IOException {
		new ShutdownThread().start();
		return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body("shutdown");
	}
	
	@RequestMapping("/status")
	public ResponseEntity<Boolean> status(HttpServletRequest req, HttpServletResponse res) throws IOException {
		return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(Stability.get());			
	}	

	@RequestMapping("/release")
	public String release(HttpServletResponse res, @RequestParam("version") String version) throws IOException {
		if ("1.0.0".equals(version)) {
			return stabilize(res);
		}
		return destabilize(res);
	}

	@RequestMapping("/stabilize")
	public String stabilize(HttpServletResponse res) throws IOException {
		Deployment.post("1.0.0");
		dataSources.free();
		Stability.set(true);
		res.sendRedirect("/");
		return "stabilized";
	}

	@RequestMapping("/destabilize")
	public String destabilize(HttpServletResponse res) throws IOException {
		Deployment.post("1.0.1");
		Stability.set(false);
		res.sendRedirect("/");
		return "destabilized";
	}
	
}