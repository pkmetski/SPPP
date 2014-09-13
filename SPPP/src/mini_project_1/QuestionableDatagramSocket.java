package mini_project_1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

public class QuestionableDatagramSocket extends DatagramSocket {

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

	@SuppressWarnings("incomplete-switch")
	public void send(DatagramPacket p) throws IOException {
		// TODO: mess up the collection of aggregated packets here
		if (this.nextError == ErrorMode.None)
			this.nextError = getNextError();
		boolean send = true;

		// is it time for another error
		if (makeError()) {
			boolean incrementErrors = true;
			switch (nextError) {
			case Drop:
				send = false;
				break;
			case Duplicate:
				super.send(p);
				break;
			case Swap:
				if (previousPacket != null) {
					super.send(previousPacket);
				} else {
					send = false;
					incrementErrors = false;
				}
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

	private boolean makeError() {
		return (ERRORS_TOTAL / PACKETS_TOTAL) * 100.0 < ERRORS_PERC;
	}

	private ErrorMode getNextError() {
		int randomNum = this.rnd.nextInt(ErrorMode.values().length - 1) + 1;
		return ErrorMode.values()[randomNum];
	}
}

enum ErrorMode {
	None, Drop, Swap, Duplicate
}
