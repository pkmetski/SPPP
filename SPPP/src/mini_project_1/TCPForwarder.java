package mini_project_1;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPForwarder {

	public static void main(String[] args) throws Exception {
		ServerSocket listenSocket = null;
		try {

			int serverPort = Integer.parseInt(args[1]);
			int destPort = Integer.parseInt(args[2]);
			InetAddress destAddress = InetAddress.getByName(args[0]);

			listenSocket = new ServerSocket(serverPort);
			Socket destSocket = new Socket(destAddress, destPort);
			while (true) {
				System.out.println("Server started. Waiting for connections.");
				Socket srcSocket = listenSocket.accept();
				System.out.println("Accepted connection.");

				// this creates two threads for bidirectional communication
				// between client and server
				new Forwarder(srcSocket, destSocket);
				new Forwarder(destSocket, srcSocket);
			}
		} finally {
			if (listenSocket != null)
				listenSocket.close();
		}
	}
}

/*
 * Forwards data from source socket input stream to destination socket output
 * stream
 */
class Forwarder extends Thread {
	private Socket srcSocket;
	private DataInputStream srcIn;
	private Socket destSocket;
	private DataOutputStream destOut;
	private volatile boolean running = true;

	public Forwarder(Socket srcSocket, Socket destSocket) throws Exception {
		this.srcSocket = srcSocket;
		this.srcIn = new DataInputStream(srcSocket.getInputStream());
		this.destSocket = destSocket;
		this.destOut = new DataOutputStream(destSocket.getOutputStream());

		this.start();
	}

	public void run() {
		try {
			byte[] buffer;
			while (running) {
				// max packet size is 65535 bytes
				buffer = new byte[65535];
				// read incoming data from source input
				srcIn.read(buffer);
				// write same data to destination output
				destOut.write(buffer);

				Thread.sleep(1000);
			}

		} catch (Exception e) {
			System.out.println("Connection died:" + e.getMessage());
		} finally {
			try {
				srcSocket.close();
				destSocket.close();
			} catch (IOException e) {
				System.out.println("Close failed: " + e.getMessage());
			}
		}
	}

	public void terminate() {
		this.running = false;
	}
}
