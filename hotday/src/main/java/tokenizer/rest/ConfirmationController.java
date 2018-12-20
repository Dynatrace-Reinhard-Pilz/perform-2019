package tokenizer.rest;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tokenizer.Stability;

@RestController("confirmation-service")
public class ConfirmationController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmationController.class);
	
		public ConfirmationController(ApplicationContext ctx) {
		Objects.requireNonNull(ctx);
	}
	
	@RequestMapping("/confirm")
	public ResponseEntity<Boolean> confirm(HttpServletRequest req, HttpServletResponse res) throws IOException {
		LOGGER.info("Received /confirm request");
		
		String sStable = req.getHeader(Stability.HTTP_HEADER);
		if ("true".equals(sStable)) {
			LOGGER.info("Application STABILIZED via confirmation request");
			Stability.set(true);
		} else if ("false".equals(sStable)) {
			LOGGER.info("Application DESTABILIZED via confirmation request");
			Stability.set(false);
		}
		
		return ResponseEntity.ok().cacheControl(CacheControl.noCache()).build();			
	}
	
}