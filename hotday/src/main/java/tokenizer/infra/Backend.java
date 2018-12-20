package tokenizer.infra;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import tokenizer.config.Options;
import tokenizer.util.Http;

public final class Backend {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Backend.class);
	
	private static final String BACKEND_PUBLISH_LOCATION = "https://friendpaste.com/23mfMAK6mdThMpskrp4muQ/";
	
	private final String backendHostAndPort;
	private final Options options;
	
	public Backend(ApplicationContext ctx) {
		Objects.requireNonNull(ctx);
		
		this.options = ctx.getBean(Options.class);
		
		switch (options.mode()) {
		case Backend:
			publish();
			this.backendHostAndPort = null;
			break;
		case Frontend:
			this.backendHostAndPort = fetchBackendHostAndPort();
			break;
		default:
			this.backendHostAndPort = null;	
		}
	}
	
	public URL validationURL() {
		if (backendHostAndPort == null) {
			return null;
		}
		try {
			return new URL(this.backendHostAndPort + "/validate");
		} catch (MalformedURLException e) {
			LOGGER.warn("invalid validation URL", e);
			return null;
		}
	}
	
	private String fetchBackendHostAndPort() {
		try {
			return Http.getraw(BACKEND_PUBLISH_LOCATION + "raw");
		} catch (IOException e) {
			LOGGER.warn("Unable to fetch backend host", e);
			return null;
		}
	}
	
	private void publish() {
		try {
			String payload = "http://" + options.fqdn() + ":" + options.listenPort();
			LOGGER.info("Publishing " + payload);
			Http.post(BACKEND_PUBLISH_LOCATION + "edit", URLEncoder.encode("paste_snippet", "UTF-8") + "=" + URLEncoder.encode(payload, "UTF-8") + "&" + URLEncoder.encode("paste_language", "UTF-8") + "=" + URLEncoder.encode("text", "UTF-8"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
