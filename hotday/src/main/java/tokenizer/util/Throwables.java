package tokenizer.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Throwables {

	private Throwables() {
		// prevent instantiation
	}
	
	public static String toString(Throwable thrown) {
		if (thrown == null) {
			return "";
		}
		String result = "";
		StringWriter sw = null;
		PrintWriter pw = null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			thrown.printStackTrace(pw);
			pw.flush();
			sw.flush();
			result = sw.getBuffer().toString();
		} finally {
			Closeables.close(sw, pw);
		}
		return result;
	}	
}
