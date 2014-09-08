package mini_project_1;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;

public class UDPForwarder {

	public static void main(String[] args) throws Exception {
		DatagramSocket aSocket = null;
		try {
			int serverPort = Integer.parseInt(args[1]);
			int destPort = Integer.parseInt(args[2]);
			InetAddress destAddress = InetAddress.getByName(args[0]);

			// create socket at agreed port
			aSocket = new DatagramSocket(serverPort);
			System.out.println(new Date()+" UDPForwarder: Listening at " + aSocket.getLocalAddress()
					+ ":" + aSocket.getLocalPort());
			// max packet size is 65535 bytes
			byte[] buffer = new byte[65535];
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer,
						buffer.length);
				System.out.println(new Date()+" UDPForwarder: Listening...");
				aSocket.receive(request);

				System.out.println(new Date()+" UDPForwarder: Received " + request.getLength() + " bytes"
						+ " from " + request.getAddress().toString() + ":"
						+ request.getPort());

				/* The forwarding part */
				/* Ignore any traffic from h */
				if (request.getPort() != destPort) {
					DatagramPacket forwardedPacket = new DatagramPacket(
							request.getData(), request.getLength(),
							destAddress, destPort);
					aSocket.send(forwardedPacket);

					System.out.println(new Date()+" UDPForwarder: Forwarding to: " + destAddress + " at "
							+ destPort);
				}
			}
		} catch (SocketException e) {
			System.out.println(new Date()+" UDPForwarder: Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println(new Date()+" UDPForwarder: IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}
}
