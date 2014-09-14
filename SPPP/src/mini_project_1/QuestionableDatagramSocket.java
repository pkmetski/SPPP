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

	enum ErrorMode {
		None, Drop, Swap, Duplicate
	}

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

	// a flag indicating whether one packet has been collected prior to
	// receiving the current one
	boolean swappable = false;

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
				// if swappable flag has been set, then there is already a
				// previous packet, if not save current packet and don't send it
				if (!swappable) {
					this.previousPacket = p;
					send = false;
					incrementErrors = false;
					// on the next call to send, the packets will be swapped
					swappable = true;
				} else {
					super.send(p);
					PACKETS_TOTAL++;
					p = previousPacket;
					swappable = false;
				}

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
