package hotday.util;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;

public final class Closeables {

	private Closeables() {
		
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
	
	public static String resource(String name) throws IOException {
		try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name)) {
			return readString(in);
		}
	}
}
