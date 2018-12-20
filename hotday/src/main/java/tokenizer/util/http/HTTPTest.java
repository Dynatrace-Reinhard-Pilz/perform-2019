package tokenizer.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Objects;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;

import tokenizer.util.Closeables;

public class HTTPTest {
	
	private final URL url;

	public static void main(String[] args) {
		try {
//			new HTTPTest("https://friendpaste.com/23mfMAK6mdThMpskrp4muQ/raw").get();
			String result = new HTTPTest("http://ec2-52-41-83-14.us-west-2.compute.amazonaws.com:8080/").get();
			System.out.println(result);
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}
	}
	
	public HTTPTest(URL url) {
		Objects.requireNonNull(url);
		
		this.url = url;
	}
	
	public HTTPTest(String url) throws MalformedURLException {
		this(new URL(Objects.requireNonNull(url)));
	}
	
	public String get() throws IOException {
		try (Socket socket = Closeables.openSocket(url)) {
			try (OutputStream out = socket.getOutputStream()) {
				Closeables.println(out, "GET" + " " + Closeables.getPath(url) + " HTTP/1.1", "Host: " + url.getHost() + ":" + Closeables.getPort(url));
				Closeables.println(out);
				
				boolean isChunked = false;
				try (InputStream in = socket.getInputStream()) {
					String line = readLine(in);
					while (line.length() > 0) {
						System.out.println(line);
						if (line.startsWith("Transfer-Encoding: chunked")) {
							isChunked = true;
						} else if (line.startsWith("Content-Length:"))
						line = readLine(in);	
					}
					
					try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
						if (isChunked) {
							int chars = readHex(in);
							while (chars > 0) {
								Closeables.copy(in, bout, chars);	
								chars = readHex(in);
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
	
	private int readHex(InputStream in) throws IOException {
		String line = readLine(in);
		if (line.trim().length() == 0) {
			line = readLine(in);
		}
		return Integer.parseInt(line, 16);
	}
	
	private String readLine(InputStream in) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			int read = in.read();
//			System.out.println(String.valueOf((char) read));
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
