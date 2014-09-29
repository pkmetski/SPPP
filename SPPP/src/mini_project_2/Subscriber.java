package mini_project_2;

import java.io.DataInputStream;
import java.net.Socket;

public class Subscriber {
	private static boolean running = true;

	public static void main(String args[]) throws Exception {
		Socket s = null;
		while (running) {
			s = new Socket(Configuration.PUBLISH_SUBSCRIBE_SYSTEM_ADDRESS,
					Configuration.PUBLISH_SUBSCRIBE_SYSTEM_SUBSCRIBER_PORT);
			DataInputStream in = new DataInputStream(s.getInputStream());
			System.out.println("Waiting on messages.");
			String message = in.readUTF();
			System.out.println("Message received: " + message);
		}
		s.close();
	}

	public void stop() {
		running = false;
	}
}
