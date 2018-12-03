package hotday;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;

public final class Closeables {

	private Closeables() {
		
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
}
