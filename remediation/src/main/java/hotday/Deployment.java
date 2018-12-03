package hotday;

import java.io.IOException;
import java.io.InputStream;

public final class Deployment {

	public static void post(String version) {
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
