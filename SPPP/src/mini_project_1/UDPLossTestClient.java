package mini_project_1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UDPLossTestClient {

	public static void main(String[] args) {
		int port = 6789;
		String server = "localhost";
		int datagramSize = Integer.parseInt(args[0]);
		int datagramCount = Integer.parseInt(args[1]);
		int transmissionInterval = Integer.parseInt(args[2]);

		runTests(datagramSize, datagramCount, transmissionInterval, server,
				port);
	}

	private static void runTests(int datagramSize, int datagramCount,
			int interval, String host, int port) {
		Map<String, Boolean> requestsSent = new HashMap<String, Boolean>(
				datagramCount);
		int repliesCount = 0;
		int duplicatesCount = 0;
		InetAddress aHost;
		DatagramSocket aSocket;
		try {
			aHost = InetAddress.getByName(host);
			aSocket = new DatagramSocket();
			// set the socket timeout
			aSocket.setSoTimeout(100);
			for (int i = 0; i < datagramCount; i++) {

				/* Prepare request */
				String requestMessage = MyStringRandomGen
						.generateRandomString(datagramSize);

				byte[] requestBuffer = requestMessage.getBytes();

				DatagramPacket request = new DatagramPacket(requestBuffer,
						requestBuffer.length, aHost, port);

				// save the request message
				requestsSent.put(requestMessage, false);

				// send the request
				aSocket.send(request);

				/* Listen for reply */
				byte[] replyBuffer = new byte[datagramSize];
				DatagramPacket reply = new DatagramPacket(replyBuffer,
						replyBuffer.length);
				try {
					aSocket.receive(reply);
				} catch (SocketTimeoutException ex) {
					// if the socket timeout expires, continue the loop
					continue;
				}
				String replyMessage = new String(reply.getData());

				if (requestsSent.containsKey(replyMessage)) {
					if (requestsSent.get(replyMessage) == false) {
						requestsSent.put(replyMessage, true);
					} else {
						duplicatesCount++;
					}
					// the reply count is increased only if a corresponding
					// request exists
					repliesCount++;
				}
				System.out.println("Reply: " + replyMessage);

				Thread.sleep(interval);
			}
			long lossPerc = ((datagramCount - repliesCount) / datagramCount) * 100;
			long duplicatePerc = (duplicatesCount / datagramCount) * 100;

			System.out
					.println(String
							.format("Total datagrams sent: %s; replies received: %s; loss %%: %s",
									datagramCount, repliesCount, lossPerc));
			System.out
					.println(String
							.format("Total datagrams sent: %s; duplicates received: %s; duplicates %%: %s",
									datagramCount, duplicatesCount,
									duplicatePerc));

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

/*
 * Code taken from
 * http://www.java2novice.com/java-collections-and-util/random/string/
 */
class MyStringRandomGen {

	private static final String CHAR_LIST = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

	/**
	 * This method generates random string
	 * 
	 * @return
	 */
	public static String generateRandomString(int stringLength) {

		StringBuffer randStr = new StringBuffer();
		for (int i = 0; i < stringLength; i++) {
			int number = getRandomNumber();
			char ch = CHAR_LIST.charAt(number);
			randStr.append(ch);
		}
		return randStr.toString();
	}

	/**
	 * This method generates random numbers
	 * 
	 * @return int
	 */
	private static int getRandomNumber() {
		int randomInt = 0;
		Random randomGenerator = new Random();
		randomInt = randomGenerator.nextInt(CHAR_LIST.length());
		if (randomInt - 1 == -1) {
			return randomInt;
		} else {
			return randomInt - 1;
		}
	}
}
