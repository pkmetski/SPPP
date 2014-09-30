package mini_project_2;

import java.io.DataOutputStream;
import java.net.Socket;

public class Publisher {
	private static boolean running = true;

	public static void main(String args[]) throws Exception {
		Socket s = new Socket(Configuration.PUBLISH_SUBSCRIBE_SYSTEM_ADDRESS, 
				Configuration.PUBLISH_SUBSCRIBE_SYSTEM_PUBLISHER_PORT);
		DataOutputStream out = new DataOutputStream(s.getOutputStream());
		while (running) {
			System.out.println("Publish a message: ");
			String message = System.console().readLine();
			out.writeUTF(message); // UTF is a string encoding see Sec. 4.4
		}
		s.close();
	}

	public void stop() {
		running = false;
	}
}
