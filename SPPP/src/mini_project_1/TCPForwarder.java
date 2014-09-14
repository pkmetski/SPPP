package mini_project_1;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * TCPForwarder
 *
 */
public class TCPForwarder {

	/**
	 * Main
	 * 
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		int serverPort = Integer.parseInt(args[0]);
		InetAddress destAddress = InetAddress.getByName(args[1]);
		int destPort = Integer.parseInt(args[2]);
		boolean running = true;
		
		ServerSocket serverSocket = new ServerSocket(serverPort);
		System.out.println(new Date()+" INFO TCPForwarder: Created server for port "+serverPort+"...");
		
		while (running) {
			try {
				Socket clientSocket = serverSocket.accept();
				System.out.println(new Date()+" INFO TCPForwarder: Accept connection...");
				TCPForwarderConnection tcpForwarderConnection = new TCPForwarderConnection(clientSocket, destAddress, destPort);		
				tcpForwarderConnection.start();
			} catch (IOException e) {
				System.out.println(new Date()+" ERROR TCPForwarder: Connection died!\n" + e.getMessage());
				running = false;
			}
		}
		
		if (serverSocket != null){
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out.println(new Date()+" ERROR TCPForwarder: Could not close server socket!\n"+e.getMessage());
			}
			System.out.println(new Date()+" INFO TCPForwarder: Server socket closed!\n");
		}
		
	}
	
}

/**
 * TCPForwarderConnection
 * 
 * Opens a TCP connection between client and forwarder and forwarder and destination.
 * Each TCP connection has an input stream for reading and an output steam for writing.
 *
 */
class TCPForwarderConnection extends Thread{
	private InetAddress destAddress;
	private int destPort;
	private Socket clientSocket = null;
	private Socket destSocket = null;
	private volatile boolean running = true;
	
	public TCPForwarderConnection(Socket clientSocket, InetAddress destAddress, int destPort){
		this.clientSocket = clientSocket;
		this.destAddress = destAddress;
		this.destPort = destPort;
	}
	
	public void run(){
		try {
			while (running) {
				destSocket = new Socket(destAddress, destPort);
				//System.out.println(new Date()+" INFO TCPForwarderConnection: Destination socket opened...");
				//System.out.println(new Date()+" INFO TCPForwarderConnection: Start TCP connection to client...");
				DataInputStream clientSocketIn = new DataInputStream(clientSocket.getInputStream());
				DataOutputStream destSocketOut = new DataOutputStream(destSocket.getOutputStream());
				Forwarder clientToDestConnection = new Forwarder(this,clientSocketIn,destSocketOut);
				clientToDestConnection.start();
				//System.out.println(new Date()+" INFO TCPForwarderConnection: Start TCP connection to destination...");
				DataInputStream destSocketIn = new DataInputStream(destSocket.getInputStream());
				DataOutputStream clientSocketOut = new DataOutputStream(clientSocket.getOutputStream());
				Forwarder destToClientConnection = new Forwarder(this,destSocketIn,clientSocketOut);
				destToClientConnection.start();
				try {
					//System.out.println(new Date()+" INFO TCPForwarderConnection: Sleeping...");
					Thread.sleep(1000); //Limits the number of loops per time unit for relief of the CPU
				} catch (InterruptedException e) {
					System.out.println(new Date()+" ERROR TCPForwarderConnection: Sleeping failed!" + e.getMessage());
				}
			}
		} catch(IOException e){
			System.out.println(new Date()+" INFO TCPForwarderConnection: Connection died!\n" + e.getMessage());
		} finally {
			terminate();
		}
	}
	
	public synchronized void terminate(){
		if (destSocket != null){
			try {
				destSocket.close();
			} catch (IOException e) {
				System.out.println(new Date()+" ERROR TCPForwarder: Could not close destination socket!\n"+e.getMessage());
			}
			//System.out.println(new Date()+" INFO TCPForwarder: Destination socket closed!");
		}
		if (clientSocket != null){
			try {
				clientSocket.close();
			} catch (IOException e) {
				System.out.println(new Date()+" ERROR TCPForwarder: Could not close client socket!\n"+e.getMessage());
			}
			//System.out.println(new Date()+" INFO TCPForwarder: Client socket closed!");
		}
		running = false;
	}
}

/**
 * Forwarder
 * 
 * Does the actual forwarding of the streams between client and destination.
 * 
 * @author Theresa
 *
 */
class Forwarder extends Thread {
	// max packet size is 65535 bytes
	private static final int BUFFER_SIZE = 65535;
	TCPForwarderConnection parent;
	private DataInputStream socketIn; //reader
	private DataOutputStream socketOut; //writer
	private volatile boolean running = true;
    
	public Forwarder(TCPForwarderConnection parent, DataInputStream socketIn, DataOutputStream socketOut){
		this.parent = parent;
		this.socketIn = socketIn;
		this.socketOut = socketOut;
		//System.out.println(new Date()+" INFO TCPConnection: In/Output streams connected...");
	}

	public void run() {
		byte[] buffer = new byte[BUFFER_SIZE];
		try {
			int bytesRead = 0;
			while(running) {
				// Reading stream
				//System.out.println(new Date()+" INFO TCPConnection: Listening for reading...");
				bytesRead = socketIn.read(buffer);
				//System.out.println(new Date()+" INFO TCPConnection: Reading...");
				if (bytesRead==-1)
					terminate();
				else {
					// Writing stream
					//System.out.println(new Date()+" INFO TCPConnection: Listening for writing...");
					socketOut.write(buffer, 0, bytesRead);
					//System.out.println(new Date()+" INFO TCPConnection: Writing...");
					socketOut.flush();
					
					yield(); //Gives other threads the chance to do something
					
					try {
						//System.out.println(new Date()+" INFO TCPConnection: Sleeping...");
						Thread.sleep(1000); //Limits the number of loops per time unit for relief of the CPU
					} catch (InterruptedException e) {
						System.out.println(new Date()+" ERROR TCPConnection: Sleeping failed!" + e.getMessage());
					}
				}
			}
		} catch (IOException e){
			//System.out.println(new Date()+" INFO TCPConnection: Read/Write stopped!\n" + e.getMessage());
		} finally {
			parent.terminate();
		}
	}

    public void terminate() {
        running = false;
    }
    
}
