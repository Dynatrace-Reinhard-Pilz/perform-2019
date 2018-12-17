package tokenizer.util;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

public final class Network {
	
	private Network() {
		// prevent instantiation
	}
	
	public static String localIP() throws IOException {
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			return socket.getLocalAddress().getHostAddress();
		}		
	}

}
