package tokenizer.rest;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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
import tokenizer.util.Http;

@RestController("validation-service")
public class ValidationController implements ThreadFactory {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationController.class);
	
	private final ExecutorService threadPool = Executors.newCachedThreadPool(this);
	
	private final DataSources dataSources;
	private final Options options;
	
	public ValidationController(ApplicationContext ctx) {
		Objects.requireNonNull(ctx);
		
		options = ctx.getBean(Options.class);
		dataSources = ctx.getBean(DataSources.class);
	}
	
	private int listenPort(HttpServletRequest req) {
		int listenPort = options.listenPort();
		String sListenPort = req.getHeader(Options.HEADER_LISTEN_PORT);
		if (sListenPort != null) {
			try {
				listenPort = Integer.parseInt(sListenPort); 
			} catch (Throwable t) {
				LOGGER.warn("invalid listen port '" + sListenPort + "'. Using '" + options.listenPort() + "' as fallback");
			}
		} else {
			LOGGER.warn("missing listen port. Using '" + options.listenPort() + "' as fallback");
		}
		return listenPort;
	}

	@RequestMapping("/validate")
	public ResponseEntity<String> validate(final HttpServletRequest req, HttpServletResponse res) throws IOException {
		LOGGER.info("Received /validate request");
		final String dynatrace = req.getHeader("X-dynaTrace");
		String remoteHost = req.getRemoteHost();
		ResponseEntity<String> response = request(req, res);
		threadPool.execute(() -> {
			try {
				Http.get(
					"http://" + remoteHost + ":" + listenPort(req) + "/confirm",
					(con) -> {
						if (dynatrace != null) {
							con.setRequestProperty("X-dynaTrace", dynatrace);
						}
					}
				);
			} catch (IOException e) {
				// ignore
			}			
		});
		
		return response;
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
	
	
	@Override
	public Thread newThread(Runnable r) {
		Thread thread = new Thread(r, "async-http");
		thread.setDaemon(true);
		return thread;
	}	
	
}