package tokenizer.config;

import java.net.HttpURLConnection;
import java.net.InetAddress;
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
	private String fqdn = null;
	private static final Object lock = new Object();
	
	public Options(ApplicationContext ctx) {
		Objects.requireNonNull(ctx);
		
		this.ctx = ctx;
//		this.fqdn = fetchPublicHostName();
	}
	
	public ServiceMode mode() {
		LOGGER.info("service.mode: " + ctx.getEnvironment().getProperty(SERVICE_MODE, "frontend"));
		return ServiceMode.resolve(ctx.getEnvironment().getProperty(SERVICE_MODE, "frontend"));
	}
	
	public String fqdn() {
		synchronized (lock) {
			if (fqdn == null) {
				fqdn = getFQDN();
				LOGGER.info("FQDN: " + fqdn);
			}
		}
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
	
	public String getFQDN() {
		try {
			String echo = Http.get("https://postman-echo.com/ip");
			int idx = echo.lastIndexOf('"');
			echo = echo.substring(0, idx);
			idx = echo.lastIndexOf('"');
			echo = echo.substring(idx + 1);
			InetAddress addr = InetAddress.getByName(echo);
			echo = addr.getHostName();
			return echo;
		} catch (Throwable t) {
			return null;
		}
	}
	
//	private String fetchPublicHostName() {
//		String host = null;
//		try {
//			host = Http.get("http://169.254.169.254/latest/meta-data/public-hostname");
//		} catch (IOException e) {
//			// ignore
//			// outside EC2 that call won't work
//			host = "";
//		}
//		if ("".equals(host)) {
//			try {
//				host = Http.get("http://169.254.169.254/latest/meta-data/public-ipv4");
//			} catch (IOException e) {
//				// ignore
//				// outside EC2 that call won't work
//				return "localhost";
//			}			
//		}
//		return host;
//	}

	@Override
	public void accept(HttpURLConnection con) {
		if (con == null) {
			return;
		}
		con.setRequestProperty(HEADER_LISTEN_PORT, String.valueOf(listenPort()));
	}
	
}
