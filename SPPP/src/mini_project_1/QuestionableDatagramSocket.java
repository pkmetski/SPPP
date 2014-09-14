package mini_project_1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

/**
 * QuestionableDatagramSocket
 *
 */
public class QuestionableDatagramSocket extends DatagramSocket {

	enum ErrorMode {None, Drop, Swap, Duplicate}
	private final int ERRORS_PERC = 40;
	private int PACKETS_TOTAL = 0;
	private int ERRORS_TOTAL = 0;
	private final Random rnd = new Random();
	private ErrorMode nextError = ErrorMode.None;
	private DatagramPacket previousPacket = null;

	public QuestionableDatagramSocket(int arg0, InetAddress arg1)
			throws SocketException {
		super(arg0, arg1);
	}

	public void send(DatagramPacket p) throws IOException {
		// Flag for sending datagram packet
		boolean send = true;
		
		// Get random error mode
		if (this.nextError == ErrorMode.None)
			this.nextError = getNextError();
		
		// If it is time for another error, do a random error
		if (makeError()) {
			// Flag for if the error count should be incremented
			boolean incrementErrors = true;
			
			switch (nextError) {
				case Drop:
					// If errorMode=Drop, no packet is discarded
					send = false;
					break;
				case Duplicate:
					// If errorMode=Duplicate, the packet is sent twice
					super.send(p);
					break;
				case Swap:
					// If errorMode=Swap, the previous packet is sent again after the current packet
					if (previousPacket != null) {
						super.send(p);
						super.send(previousPacket);
					} else {
						incrementErrors = false;
					}
					send = false;
					break;
				default:
					break;
			}
			if (incrementErrors) {
				ERRORS_TOTAL++;
				this.nextError = ErrorMode.None;
			}
		}
		if (send) {
			super.send(p);
		}
		this.previousPacket = p;
		PACKETS_TOTAL++;
	}

	// Calculates if another error should be made
	private boolean makeError() {
		return (ERRORS_TOTAL / PACKETS_TOTAL) * 100.0 < ERRORS_PERC;
	}

	// Returns a random error mode
	private ErrorMode getNextError() {
		int randomNum = this.rnd.nextInt(ErrorMode.values().length - 1) + 1;
		return ErrorMode.values()[randomNum];
	}
}
