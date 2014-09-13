package mini_project_1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javax.naming.directory.InvalidAttributeValueException;

import mini_project_1.Message.MessageType;

public class UDPReliableClient {

	public static void main(String[] args) {

		// args give message contents and destination hostname
		DatagramSocket aSocket = null;
		DatagramPacket lastPacket = null;
		try {
			aSocket = new DatagramSocket();
			aSocket.setSoTimeout(1000);// set a timeout of 1000 ms
			InetAddress aHost = InetAddress.getByName(args[0]);
			int serverPort = Integer.parseInt(args[1]);
			byte[] messageBytes = args[2].getBytes();

			// split the original message into chunks
			Message[] messagesToSend = createMessages(messageBytes,
					UDPReliableMessageTranslator.CHUNK_SIZE);

			// create the connect message
			Message initMsg = new Message(MessageType.RequestConnect, 0,
					new byte[0]);
			byte[] data = UDPReliableMessageTranslator.getBytes(initMsg);
			lastPacket = new DatagramPacket(data, data.length, aHost,
					serverPort);
			aSocket.send(lastPacket);

			// start listening on messages from the server
			boolean done = false;
			while (!done) {

				byte[] buffer = new byte[UDPReliableMessageTranslator.HEADER_SIZE
						+ UDPReliableMessageTranslator.CHUNK_SIZE];
				DatagramPacket serverPacket = new DatagramPacket(buffer,
						buffer.length);
				// in case the packet was lost, the server will not reply,
				// resend it
				boolean sent = false;
				do {
					try {
						aSocket.receive(serverPacket);
						sent = true;
					} catch (SocketTimeoutException tex) {
						aSocket.send(lastPacket);
						System.out.println("Resending packet");
					}
				} while (!sent);
				System.out.println("Received message from server");

				// determine the server message
				Message serverMessage = UDPReliableMessageTranslator
						.getMessage(buffer);
				Message clientMessage = null;
				switch (serverMessage.getMessageType()) {
				case RequestData:
					// depending on the request data msg, send the appropriate
					// byte chunk sequence
					int chunk = serverMessage.getByteIndex()
							/ UDPReliableMessageTranslator.CHUNK_SIZE;
					if (chunk < messagesToSend.length) {
						clientMessage = messagesToSend[chunk];
					} else {
						clientMessage = new Message(MessageType.EndData, 0,
								new byte[1]);
					}
					break;
				case ConfirmReceipt:
					System.out.println("All data transferred!");
					done = true;
					break;
				default:
					throw new InvalidAttributeValueException();
				}
				
				if (clientMessage != null) {
					byte[] data2 = UDPReliableMessageTranslator
							.getBytes(clientMessage);
					lastPacket = new DatagramPacket(data2, data2.length, aHost,
							serverPort);
					aSocket.send(lastPacket);
				}
			}

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} catch (InvalidAttributeValueException e) {
			System.out.println("Server replied with unexpected message type.");
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}

	// split the original data into message objects
	private static Message[] createMessages(byte[] data, int chunkSize) {

		int msgCount = (int) Math.ceil(data.length / (double) chunkSize);
		Message[] messages = new Message[msgCount];

		for (int startIndex = 0; startIndex < data.length; startIndex += chunkSize) {
			int endIndex = Math.min(data.length - 1,
					(startIndex + chunkSize) - 1);
			int length = (endIndex - startIndex) + 1;
			byte[] arr = new byte[length];
			System.arraycopy(data, startIndex, arr, 0, arr.length);
			MessageType msgType = null;
			if (startIndex + chunkSize < data.length) {
				msgType = MessageType.Data;
			} else {
				msgType = MessageType.EndData;
			}
			messages[startIndex / chunkSize] = new Message(msgType, startIndex,
					arr);
		}

		return messages;
	}
}
