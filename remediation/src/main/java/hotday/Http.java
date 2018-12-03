package hotday;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class Http {
	
	private Http() {
		
	}
	
	public static void get(String sUrl) {
		try {
			URL url = new URL(sUrl);
			try (InputStream in = url.openStream()) {
				Closeables.drain(in);
			}
		} catch (Throwable t) {
//			t.printStackTrace(System.err);
		}
	}
	
	private static HttpURLConnection openConnection(URL url) throws IOException {
		return (HttpURLConnection) url.openConnection();
	}
	
	public static void post(String sUrl, String body, String apiToken) {
		try {
			URL url = new URL(sUrl);
			HttpURLConnection con = openConnection(url);
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Authorization", "Api-Token " + apiToken);
			con.setRequestProperty("Content-Type", "application/json");
			
			try (OutputStream out = con.getOutputStream()) {
				Closeables.copy(body, out);
				try (InputStream in = con.getInputStream()) {
					Closeables.drain(in);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}		
	}

}
