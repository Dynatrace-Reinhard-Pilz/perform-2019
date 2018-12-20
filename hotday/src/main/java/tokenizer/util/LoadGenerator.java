package tokenizer.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;

import tokenizer.config.Options;

public final class LoadGenerator extends TimerTask implements CommandLineRunner {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LoadGenerator.class);

	private final Timer timer = new Timer(true);
	private final URL url;
	private final Options options;
	
	public LoadGenerator(ApplicationContext ctx, String url) {
		Objects.requireNonNull(url);
		
		options = ctx.getBean(Options.class);
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			throw new InternalError(e);
		}
	}
	
	public static void get(URL url) {
		LOGGER.info("requesting " + url);
		try {
			Http.getraw(url);
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}		
	}
	
    @Override
    public void run() {
    	get(url);
    }

	@Override
	public void run(String... args) throws Exception {
		if (options.isFrontend()) {
			timer.schedule(this, 0, 1000);
		}
	}
	
}
