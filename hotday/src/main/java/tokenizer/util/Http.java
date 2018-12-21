package tokenizer.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Objects;
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
		}
	}
	
	public static String getraw(String url) throws IOException {
		return getraw(new URL(Objects.requireNonNull(url)));
	}
	
	public static String getraw(URL url) throws IOException {
		try (Socket socket = Closeables.openSocket(url)) {
			try (OutputStream out = socket.getOutputStream()) {
				Closeables.println(out, GET + " " + Closeables.getPath(url) + " HTTP/1.1", "Host: " + url.getHost() + ":" + Closeables.getPort(url));
				Closeables.println(out);
				
				boolean isChunked = false;
				int contentLength = -1;
				try (InputStream in = socket.getInputStream()) {
					String line = readLine(in);
					while (line.length() > 0) {
						if (line.startsWith("Transfer-Encoding: chunked")) {
							isChunked = true;
						} else if (line.startsWith("Content-Length:")) {
							contentLength = Integer.parseInt(line.substring("Content-Length:".length()).trim());
						}
						line = readLine(in);	
					}
					
					try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
						if (isChunked) {
							int chars = readHex(in);
							while (chars > 0) {
								Closeables.copy(in, bout, chars);	
								chars = readHex(in);
							}
						} else if (contentLength != -1) {
							if (contentLength == 0) {
								return "";
							} else {
								Closeables.copy(in, bout, contentLength);
								return new String(bout.toByteArray());
							}
						} else {
							Closeables.copy(in, bout);
						}
						return new String(bout.toByteArray());
					}
				}
			}
		}
	}
	
	private static int readHex(InputStream in) throws IOException {
		String line = readLine(in);
		if (line.trim().length() == 0) {
			line = readLine(in);
		}
		return Integer.parseInt(line, 16);
	}
	
	private static String readLine(InputStream in) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			int read = in.read();
			while (read != -1) {
				if (read == '\r') {
					// ignore
				} else if (read == '\n') {
					return new String(out.toByteArray());
				} else {
					out.write(read);					
				}
				read = in.read();
			}
			byte[] bytes = out.toByteArray();
			if (bytes.length == 0) {
				return null;
			}
			return new String(bytes);
		}
	}	

}
