package tokenizer.events;

import java.io.IOException;
import java.io.InputStream;

import tokenizer.util.Closeables;
import tokenizer.util.Http;

public final class Deployment {
	
	private static final boolean IS_ACTIVE = "true".equals(System.getProperty("deployment.events"));

	public static void post(String version) {
		if (!IS_ACTIVE) return;
		try {
			Http.post(
					"https://siz65484.live.dynatrace.com/api/v1/events",
					resolve(
							"PROCESS_GROUP_INSTANCE-19847273D917D343",
							"SERVICE-F0F57FE79D8EC0F1",
							version
					),
					"KN7jh2l6ROOxdtYJk3KX_"
				);
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}
	}
	
	private static String resolve(String pgiId, String serviceId, String version) throws IOException {
		try (InputStream in = Deployment.class.getClassLoader().getResourceAsStream("deployment_event.json")) {
			String event = Closeables.readString(in);
			event = event.replace("$START_TIMESTAMP", String.valueOf(System.currentTimeMillis()));
			event = event.replace("$PGI_ID", pgiId);
			event = event.replace("$SERVICE_ID", serviceId);
			event = event.replace("$END_TIMESTAMP", String.valueOf(System.currentTimeMillis()));
			event = event.replace("$VERSION", "1.0");
			return event;
		}
	}
}
