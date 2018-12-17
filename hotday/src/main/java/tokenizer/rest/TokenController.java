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
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tokenizer.DataSources;
import tokenizer.config.Options;
import tokenizer.infra.Backend;
import tokenizer.util.Http;

@RestController("token-service")
public class TokenController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TokenController.class);
	
	private final DataSources dataSources;
	private final Backend backendLocation;
	private final Options options;
	
	public TokenController(ApplicationContext ctx) {
		Objects.requireNonNull(ctx);
		
		backendLocation = ctx.getBean(Backend.class);
		options = ctx.getBean(Options.class);
		dataSources = ctx.getBean(DataSources.class);
	}
	
	@RequestMapping("/token")
	public ResponseEntity<String> token(HttpServletRequest req, HttpServletResponse res) throws IOException {
		LOGGER.info("Received /token request");
		Http.get(backendLocation.validationURL(), options);
		return request(req, res);
	}
	
	public ResponseEntity<String> request(HttpServletRequest req, HttpServletResponse res) throws IOException {
		BodyBuilder builder = null;
		try {
			dataSources.run();
			builder = ResponseEntity.ok();
		} catch (Throwable t) {
			builder = ResponseEntity.status(500);
		}
		return builder.cacheControl(CacheControl.noCache()).body("response");			
	}
}