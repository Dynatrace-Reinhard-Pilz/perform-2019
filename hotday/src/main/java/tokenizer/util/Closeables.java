package tokenizer.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;

public final class Closeables {
	
	public static final OutputStream DevNull = new OutputStream() {
		
		@Override
		public void write(int b) throws IOException {
			// ignore
		}
		
	};

	private Closeables() {
		// prevent instantiation
	}
	
	public static void close(Closeable...c) {
		if (c == null) {
			return;
		}
		for (Closeable closeable : c) {
			close(closeable);
		}
	}
	
	public static void close(Closeable c) {
		if (c == null) {
			return;
		}
		try {
			c.close();
		} catch (IOException e) {
		}
	}
	
	public static Socket openSocket(URL url) throws IOException {
		int port = getPort(url);
		if (port == 443) {
	          SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
	          SSLSocket socket = (SSLSocket)factory.createSocket(InetAddress.getByName(url.getHost()), 443);
	          socket.startHandshake();
	          return socket;
		}
		return new Socket(InetAddress.getByName(url.getHost()), getPort(url));
	}
	
	public static int getPort(URL url) {
		int port = url.getPort();
		if (port == -1) {
			if ("https".equals(url.getProtocol())) {
				return 443;
			}
			port = 80;
		}
		return port;
	}
	
	public static String getPath(URL url) {
		String path = url.getPath();
		if (path.length() == 0) {
			path = "/";
		}	
		return path;
	}
	
	public static String readLine(Socket socket) throws IOException {
		Objects.requireNonNull(socket);
		
		try (InputStream in = socket.getInputStream()) {
			return readLine(in);
		}
	}
	
	public static String readLine(InputStream in) throws IOException {
		Objects.requireNonNull(in);
		
		try (InputStreamReader isr = new InputStreamReader(in)) {
			try (BufferedReader br = new BufferedReader(isr)) {
				return br.readLine();
			}
		}
	}
	
	public static String[] readLines(Socket socket) throws IOException {
		Objects.requireNonNull(socket);
		
		try (InputStream in = socket.getInputStream()) {
			return readLines(in);
		}
	}
	
	public static String[] readLines(InputStream in) throws IOException {
		Objects.requireNonNull(in);
		
		ArrayList<String> lines = new ArrayList<>();
		
		try (InputStreamReader isr = new InputStreamReader(in)) {
			try (BufferedReader br = new BufferedReader(isr)) {
				String readLine = br.readLine();
				while (readLine != null) {
					System.out.println("# " + readLine);
					lines.add(readLine);
					readLine = br.readLine();
				}
			}
		}
		return lines.toArray(new String[lines.size()]);
	}
	
	public static void println(OutputStream out) throws IOException {
		Objects.requireNonNull(out);
		
		println(out, "");
	}
	
	public static void println(OutputStream out, String line) throws IOException {
		Objects.requireNonNull(out);
		Objects.requireNonNull(line);
		
		print(out, line + "\n");
		
	}
	
	public static void println(OutputStream out, String... lines) throws IOException {
		Objects.requireNonNull(out);
		Objects.requireNonNull(lines);
		
		for (String line : lines) {
			println(out, line);
		}	
	}
	
	public static void print(OutputStream out, String line) throws IOException {
		Objects.requireNonNull(out);
		Objects.requireNonNull(line);
		
		
		out.write(line.getBytes());
		out.flush();
	}
	
	public static String readString(InputStream in) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			copy(in, out);
			return new String(out.toByteArray());
		}
	}
	
	public static void copy(String s, OutputStream out) throws IOException {
		copy(s.getBytes(), out);
	}
	
	public static void copy(byte[] bytes, OutputStream out) throws IOException {
		try (InputStream in = new ByteArrayInputStream(bytes)) {
			copy(in, out);
		}
	}
	
	public static void copy(InputStream in, OutputStream out) throws IOException {
		if (in == null) {
			return;
		}
		if (out == null) {
			return;
		}
		byte[] buffer = new byte[4096];
		int len = in.read(buffer);
		while (len != -1) {
			out.write(buffer, 0, len);
			len = in.read(buffer);
		}
		out.flush();
	}
	
	public static void drain(InputStream in) throws IOException {
		byte[] buffer = new byte[4096];
		while (in.read(buffer) != -1);
	}
	
	public static void drain(HttpURLConnection con) throws IOException {
		try (InputStream in = con.getInputStream()) {
			Closeables.drain(in);
		}		
	}
	
	public static void drain(URL url) throws IOException {
		try (InputStream in = url.openStream()) {
			Closeables.drain(in);
		}		
	}
	
	public static String resource(String name) throws IOException {
		try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name)) {
			return readString(in);
		}
	}
}
