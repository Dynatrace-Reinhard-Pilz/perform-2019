package hotday;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController6 {
	
	private static final Random RAND = new Random(System.currentTimeMillis());	
	
	private static AtomicBoolean STABLE = new AtomicBoolean(true);
	
	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			return;
		}
	}
	
	@RequestMapping("/hello/*")
	public String hello(HttpServletRequest req, HttpServletResponse res) {
		if (STABLE.get()) {
			res.setStatus(200);
			return "hello";
		} else {
			sleep(800 + RAND.nextInt(500));
			res.setStatus(200);
			return "hello";
		}
	}

	@RequestMapping("/stabilize")
	public String stabilize(HttpServletRequest req, HttpServletResponse res) {
		Deployment.post("1.0.0");
		res.setStatus(200);
		STABLE.set(true);
		return "stabilized";
	}

	@RequestMapping("/destabilize")
	public String destabilize(HttpServletRequest req, HttpServletResponse res) {
		Deployment.post("1.0.1");
		res.setStatus(200);
		STABLE.set(false);
		return "destabilized";
	}
	
}