package mini_project_1;

import mini_project_1.Message.MessageType;

public class UDPReliableMessageTranslator {

	public static final int HEADER_SIZE = 2;
	public static final int CHUNK_SIZE = 4;

	/*
	 * Message header: 1st byte - message type; 2nd byte (byte index) - next
	 * byte waiting on, or missing byte (for RequestData) or first byte in
	 * package (for Data)
	 */

	// used by client/server to translate message object into byte array, ready
	// for sending
	public static byte[] getBytes(Message message) {
		byte[] data = new byte[message.getData().length + HEADER_SIZE];
		data[0] = integerToByte(message.getMessageType().getValue());
		data[1] = integerToByte(message.getByteIndex());
		System.arraycopy(message.getData(), 0, data, HEADER_SIZE,
				message.getData().length);
		return data;
	}

	// used by client/server to translate received byte data into message
	// objects
	public static Message getMessage(byte[] arr) {
		// get first byte - message type
		int i = byteToInteger(arr[0]);
		MessageType msgType = MessageType.fromInt(i);

		// get second byte - byte index
		int byteIndex = byteToInteger(arr[1]);

		// get the data
		byte[] data = new byte[arr.length - HEADER_SIZE];
		System.arraycopy(arr, HEADER_SIZE, data, 0, data.length);

		return new Message(msgType, byteIndex, data);
	}

	private static int byteToInteger(byte bt) {
		// int i2 = i & 0xff;
		// m[0] = (byte) 200;
		return new Byte(bt).intValue();
	}

	private static byte integerToByte(int i) {
		return new Integer(i).byteValue();
	}
}

class Message {

	public enum MessageType {
		// Sent by client upon first connect
		RequestConnect(0),
		// Sent by server for requesting next bit
		RequestData(1),
		// Sent by client with actual data
		Data(2),
		// Sent by client when this is the last message containing data
		EndData(3),
		// Sent by server upon receiving all packages
		ConfirmReceipt(4);

		private int value;

		private MessageType(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}

		public static MessageType fromInt(int value) {
			for (MessageType mt : MessageType.values()) {
				if (mt.getValue() == value) {
					return mt;
				}
			}
			return null;
		}
	}

	private final MessageType messageType;
	private final int byteIndex;
	private final byte[] data;

	public Message(MessageType messageType, int byteIndex, byte[] data) {
		this.messageType = messageType;
		this.byteIndex = byteIndex;
		this.data = data;
	}

	public MessageType getMessageType() {
		return this.messageType;
	}

	public int getByteIndex() {
		return this.byteIndex;
	}

	public byte[] getData() {
		return this.data;
	}
}
