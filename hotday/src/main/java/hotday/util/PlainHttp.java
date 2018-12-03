package hotday.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

public final class PlainHttp {

	
	private PlainHttp() {
		// prevent instantiation
	}
	
	public static void get(String sUrl) {
		try {
			URL url = new URL(sUrl);
			String path = url.getPath();
			if (path == "") {
				path = "/";
			}
			String host = url.getHost();
			int port = url.getPort();
			if (port == -1) {
				port = 80;
			}
			try (Socket socket = new Socket(InetAddress.getByName(host), port)) {
				try (OutputStream out = socket.getOutputStream()) {
					String requestLine = "GET " + path + " HTTP/1.1\n";
					out.write(requestLine.getBytes());
					requestLine = "Host: " + host + ":" + port + "\n\n";
					out.write(requestLine.getBytes());
//					pw.println("Host: stackoverflow.com");
					out.flush();
					try (InputStream in = socket.getInputStream()) {
						try (InputStreamReader isr = new InputStreamReader(in)) {
							try (BufferedReader br = new BufferedReader(isr)) {
								br.readLine();
//								System.out.println(br.readLine());
							}
						}
					}
				}
			}
		} catch (Throwable t) {
//			t.printStackTrace(System.err);
		}
		
	}
	
    public static void main(String[] args) {
    	get("https://www.google.at");
    }
	
}
