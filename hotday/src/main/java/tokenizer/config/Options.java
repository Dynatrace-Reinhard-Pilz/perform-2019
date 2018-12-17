package tokenizer.config;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import tokenizer.util.Http;

public class Options implements Consumer<HttpURLConnection>{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Options.class);
	
	public static final String SERVICE_MODE = "service.mode";
	public static final String DEFAULT_SERVICE_MODE = "service.mode";
	
	public static final String HEADER_LISTEN_PORT = "X-Listen-Port";
	
	private final ApplicationContext ctx;
	private final String fqdn;
	
	public Options(ApplicationContext ctx) {
		Objects.requireNonNull(ctx);
		
		this.ctx = ctx;
		this.fqdn = fetchPublicHostName();
	}
	
	public ServiceMode mode() {
		LOGGER.info("service.mode: " + ctx.getEnvironment().getProperty(SERVICE_MODE, "frontend"));
		return ServiceMode.resolve(ctx.getEnvironment().getProperty(SERVICE_MODE, "frontend"));
	}
	
	public String fqdn() {
		return fqdn;
	}
	
	public boolean isFrontend() {
		return mode() == ServiceMode.Frontend;
	}
	
	public boolean isBackend() {
		return mode() == ServiceMode.Backend;
	}
	
	public int listenPort() {
		return Integer.parseInt(ctx.getEnvironment().getProperty("server.port"));
	}
	
	private String fetchPublicHostName() {
		try {
			return Http.get("http://169.254.169.254/latest/meta-data/public-hostname");
		} catch (IOException e) {
			// ignore
			// outside EC2 that call won't work
			return "localhost";
		}
	}

	@Override
	public void accept(HttpURLConnection con) {
		if (con == null) {
			return;
		}
		con.setRequestProperty(HEADER_LISTEN_PORT, String.valueOf(listenPort()));
	}
	
}
