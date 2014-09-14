package mini_project_1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.naming.directory.InvalidAttributeValueException;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import mini_project_1.Message.MessageType;

public class UDPReliableServer {

	private static Map<Client, ClientState> clients = new HashMap<Client, ClientState>();

	public static void main(String[] args) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(6789);
			System.out.println("Listening at " + aSocket.getLocalAddress()
					+ ":" + aSocket.getLocalPort());
			// create socket at agreed port
			while (true) {
				byte[] buffer = new byte[UDPReliableMessageTranslator.HEADER_SIZE
						+ UDPReliableMessageTranslator.CHUNK_SIZE];
				DatagramPacket clientPacket = new DatagramPacket(buffer,
						buffer.length);
				System.out.println("Listening...");
				aSocket.receive(clientPacket);

				// get the client's state
				ClientState state = getClientState(clientPacket.getAddress(),
						clientPacket.getPort());

				Message clientMessage = UDPReliableMessageTranslator
						.getMessage(buffer);

				Message serverMessage = null;

				switch (clientMessage.getMessageType()) {
				case RequestConnect:
					// save client in order to keep track of its state
					if (saveClient(clientPacket.getAddress(),
							clientPacket.getPort())) {

						// prepare a request data message
						serverMessage = new Message(MessageType.RequestData, 0,
								new byte[1]);
					}
					break;
				case Data:
					// serve the request, only if this is a known client
					if (state != null) {
						// make sure the message received is with the index we
						// expect
						int byteIndex = clientMessage.getByteIndex();

						state.addMessage(byteIndex, clientMessage);

						serverMessage = new Message(
								MessageType.RequestData,
								byteIndex += UDPReliableMessageTranslator.CHUNK_SIZE,
								new byte[1]);
					}
					break;
				case EndData:
					if (state != null) {
						state.addMessage(clientMessage.getByteIndex(),
								clientMessage);

						// make sure all the packages are received
						int missingChunk = getNextChunkIndex(
								state.getByteIndices(),
								UDPReliableMessageTranslator.CHUNK_SIZE);

						// if everything has been received, send confirm receipt
						if (missingChunk < 0) {
							serverMessage = new Message(
									MessageType.ConfirmReceipt, -1, new byte[1]);

							byte[] bt = reconstructOriginalMessage(state
									.getMessages());

							System.out.println("Received " + new String(bt)
									+ " from "
									+ clientPacket.getAddress().toString()
									+ ":" + clientPacket.getPort());
							// remove client
							removeClient(clientPacket.getAddress(),
									clientPacket.getPort());
						}
						// if a package is missing, request it
						else {
							serverMessage = new Message(
									MessageType.RequestData, missingChunk,
									new byte[1]);
						}
					}
					break;
				default:
					throw new InvalidAttributeValueException();
				}
				if (serverMessage != null) {
					byte[] data = UDPReliableMessageTranslator
							.getBytes(serverMessage);

					DatagramPacket reply = new DatagramPacket(data,
							data.length, clientPacket.getAddress(),
							clientPacket.getPort());
					aSocket.send(reply);
				}
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} catch (InvalidAttributeValueException e) {
			System.out.println("Client replied with unexpected message type.");
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}

	// assumes the keys are ordered
	private static int getNextChunkIndex(Set<Integer> keySet, int chunkSize) {
		int nextChunk = -1;
		int current = 0;
		for (int index : keySet) {
			if (current != index) {
				return current;
			} else {
				current += chunkSize;
			}
		}
		return nextChunk;
	}

	private static boolean saveClient(InetAddress address, int port) {
		Client client = new Client(address, port);
		// add client only if it doesn't exist already
		if (!clients.containsKey(client)) {
			clients.put(client, new ClientState());
			return true;
		}
		return false;
	}

	private static void removeClient(InetAddress address, int port) {
		clients.remove(new Client(address, port));
	}

	private static ClientState getClientState(InetAddress address, int port) {
		Client client = new Client(address, port);
		return clients.get(client);
	}

	private static byte[] reconstructOriginalMessage(
			Collection<Message> messages) {
		byte[] bytes = new byte[UDPReliableMessageTranslator.CHUNK_SIZE
				* messages.size()];
		int count = 0;
		for (Message msg : messages) {
			for (byte bt : msg.getData()) {
				bytes[count++] = bt;
			}
		}
		return bytes;
	}
}

class Client {
	private InetAddress address;
	private int port;

	public Client(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}

	@Override
	public boolean equals(Object obj) {
		Client otherClient = (Client) obj;
		return this.address.equals(otherClient.address)
				&& this.port == otherClient.port;
	}

	@Override
	public int hashCode() {
		// bit simplistic, but does the job
		return this.address.getHostName().hashCode() * this.port;
	}
}

class ClientState {
	// using treemap to keep the records sorted by their keys (byte index)
	private Map<Integer, Message> messages = new TreeMap<Integer, Message>();

	public void addMessage(int byteIndex, Message message) {
		if (!this.messages.containsKey(byteIndex))
			this.messages.put(byteIndex, message);
	}

	public Set<Integer> getByteIndices() {
		return this.messages.keySet();
	}

	public Collection<Message> getMessages() {
		return this.messages.values();
	}
}
