package hotday.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotday.DataSources;
import hotday.ShutdownThread;
import hotday.Stability;
import hotday.events.Deployment;
import hotday.util.Closeables;

@RestController("main-controller-8")
public class MainController {
	
	private final DataSources dataSources;
	
	public MainController(BeanFactory ctx) {
		Objects.requireNonNull(ctx);
		dataSources = ctx.getBean(DataSources.class);
	}
	
	@RequestMapping("/index.css")
	public ResponseEntity<String> indexcss() throws IOException {
		return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(Closeables.resource("index.css"));			
	}
	
	@RequestMapping("/status")
	public ResponseEntity<String> status(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String body = "";
		if (!Stability.get()) {
			body = Closeables.resource("index.html").replace("$STABILITY", Closeables.resource("instable.htmlf")).replace("$ERROR", "");
		} else {
			body = Closeables.resource("index.html").replace("$STABILITY", Closeables.resource("stable.htmlf")).replace("$ERROR", "");
		}
		return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(body);			
	}
	
	@RequestMapping("/shutdown")
	public ResponseEntity<String> shutdown() throws IOException {
		new ShutdownThread().start();
		return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body("shutdown");
	}
	
	@RequestMapping("/**")
	public ResponseEntity<String> hello(HttpServletRequest req, HttpServletResponse res) throws IOException {
		BodyBuilder builder = null;
		String error = "";
		try {
			dataSources.run();
			builder = ResponseEntity.ok();
		} catch (Throwable t) {
			builder = ResponseEntity.status(500);
			error = toString(t);
		}
		String body = "";
		if (!Stability.get()) {
			body = Closeables.resource("index.html").replace("$STABILITY", Closeables.resource("instable.htmlf")).replace("$ERROR", error);
		} else {
			body = Closeables.resource("index.html").replace("$STABILITY", Closeables.resource("stable.htmlf")).replace("$ERROR", error);
		}
		
		return builder.cacheControl(CacheControl.noCache()).body(body);			
	}
	
	private static String toString(Throwable thrown) {
		if (thrown == null) {
			return "";
		}
		String result = "";
		StringWriter sw = null;
		PrintWriter pw = null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			thrown.printStackTrace(pw);
			pw.flush();
			sw.flush();
			result = sw.getBuffer().toString();
		} finally {
			Closeables.close(sw, pw);
		}
		return result;
	}

	@RequestMapping("/stabilize")
	public String stabilize(HttpServletResponse res) throws IOException {
		Deployment.post("1.0.0");
		dataSources.free();
		Stability.set(true);
		res.sendRedirect("/status");
		return "stabilized";
	}

	@RequestMapping("/destabilize")
	public String destabilize(HttpServletResponse res) throws IOException {
		Deployment.post("1.0.1");
		Stability.set(false);
		res.sendRedirect("/status");
		return "destabilized";
	}
	
}