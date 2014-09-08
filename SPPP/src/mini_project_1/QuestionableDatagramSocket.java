package mini_project_1;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class QuestionableDatagramSocket extends DatagramSocket {

	private final int PERC = 100;

	public QuestionableDatagramSocket(int arg0, InetAddress arg1)
			throws SocketException {
		super(arg0, arg1);

	}

	public void send(DatagramPacket p) throws IOException {
		// TODO: mess up the collection of aggregated packets here
		super.send(p);
	}
}
