package tokenizer.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.function.Consumer;

public final class Http {
	
	private static final String HEADER_AUTHORIZATION = "Authorization";
	private static final String HEADER_CONTENT_TYPE = "Content-Type";
	private static final String APPLICATION_JSON = "application/json";
	private static final String API_TOKEN = "Api-Token";
	
	private static final String POST = "POST";
	private static final String GET = "GET";
	
	private Http() {
		
	}
	
	public static String get(String url) throws IOException {
		return get(url, null);
	}
	
	public static String get(String url, Consumer<HttpURLConnection> modifier) throws IOException {
		return get(new URL(url), modifier);
	}
	
	public static String get(URL url) throws IOException {
		return get(url, null);
	}
	
	public static String get(URL url, Consumer<HttpURLConnection> modifier) throws IOException {
		if (url == null) {
			return "";
		}
		
		HttpURLConnection con = openConnection(url);
		if (modifier != null) {
			modifier.accept(con);
		}
		try (InputStream in = con.getInputStream()) {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				Closeables.copy(in, out);
				return new String(out.toByteArray());
			}
		}		
	}
	
	public static HttpURLConnection openConnection(URL url) throws IOException {
		return (HttpURLConnection) url.openConnection();
	}
	
	public static void post(String url, String body) throws IOException {
		post(url, body, null);
	}
	
	public static void post(String url, String body, String apiToken) throws IOException {
		try {
			post(new URL(url), body, apiToken);
		} catch (MalformedURLException e) {
			throw new InternalError(e);
		}
	}
	
	public static void post(URL url, String body, String apiToken) throws IOException {
		HttpURLConnection con = openConnection(url);
		con.setRequestMethod(POST);
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setUseCaches(false);
		if (apiToken != null) {
			con.setRequestProperty(HEADER_AUTHORIZATION, API_TOKEN + " " + apiToken);
			con.setRequestProperty(HEADER_CONTENT_TYPE, APPLICATION_JSON);
		}
		
		try (OutputStream out = con.getOutputStream()) {
			Closeables.copy(body, out);
			Closeables.drain(con);
		}	}
	
	public static void getraw(URL url) throws IOException {
		try (Socket socket = Closeables.openSocket(url)) {
			try (OutputStream out = socket.getOutputStream()) {
				Closeables.println(out, GET + " " + Closeables.getPath(url) + " HTTP/1.1", "Host: " + url.getHost() + ":" + Closeables.getPort(url));
				Closeables.println(out);
				Closeables.readLine(socket);
			}
		}
	}

}
