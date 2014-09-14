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
public class TCPForwarder extends Thread {
	
	protected ServerSocket serverSocket = null;
	protected Socket clientSocket = null;
	protected InetAddress destAddress;
	protected int destPort;
	private Socket destSocket = null;
	protected boolean running = true;

	public static void main(String[] args) throws IOException, InterruptedException {
		
		// Parameter check
		if (args.length < 1) {
			System.out.println (new Date()+" ERROR TCPForwarder: Usage: TCPForwarder <Serverport> <DestinationAddress> <DestinationPort>");
			System.exit(-1);
		}
		
		// Input Port
		int serverPort = Integer.parseInt(args[0]);
		
		// Input destination address
		InetAddress destAddress = InetAddress.getByName(args[1]);
		
		// Input destination port
		int destPort = Integer.parseInt(args[2]);
		
		new TCPForwarder(serverPort, destAddress, destPort);
	}
	
	public TCPForwarder(int serverPort, InetAddress destAddress, int destPort) {
		
		this.destAddress = destAddress;
		this.destPort = destPort;
		
		try {
			serverSocket = new ServerSocket(serverPort);
			System.out.println(new Date()+" INFO TCPForwarder: Created server for port "+serverPort+"...\n");
		} catch(IOException e){
			System.out.println(new Date()+" ERROR TCPForwarder: Could not create the server port!\n" + e.getMessage());
			return;
		}

		// Because serverSocket.accept is a blocking call, which will be only ended if a client is connecting to 
		// the server, this call is wrapped into a thread.
		start();		
	}
	
	public void run(){
		while(running){
			try{
				// Waiting for a client to connect
				clientSocket = serverSocket.accept();
				System.out.println (new Date()+" INFO TCPForwarder:\n--------------------TCP--------------------\n");
				System.out.println(new Date()+" INFO TCPForwarder: Client ("+clientSocket.getLocalAddress()+":"+clientSocket.getLocalPort()+") made a connection...");
				
				// Check if connection is valid
				if (serverSocket.getInetAddress().equals(destAddress) && serverSocket.getLocalPort()==destPort){
					System.out.println (new Date()+" INFO TCPForwarder: Destination equals TCPForwarder -- No forwarding made!");
				} else {
					// Opens a TCP connection between client and forwarder and forwarder and destination.
					// Each TCP connection has an input stream for reading and an output steam for writing.
					destSocket = new Socket(destAddress, destPort);
					//System.out.println(new Date()+" INFO TCPForwarderConnection: Destination socket opened...");
					//System.out.println(new Date()+" INFO TCPForwarderConnection: Start TCP connection to client...");
					DataInputStream clientSocketIn = new DataInputStream(clientSocket.getInputStream());
					DataOutputStream destSocketOut = new DataOutputStream(destSocket.getOutputStream());
					Forwarder clientToDestConnection = new Forwarder(clientSocket.getLocalAddress()+":"+clientSocket.getLocalPort(),clientSocketIn,destSocketOut);
					clientToDestConnection.start();
					//System.out.println(new Date()+" INFO TCPForwarderConnection: Start TCP connection to destination...");
					DataInputStream destSocketIn = new DataInputStream(destSocket.getInputStream());
					DataOutputStream clientSocketOut = new DataOutputStream(clientSocket.getOutputStream());
					Forwarder destToClientConnection = new Forwarder(destSocket.getLocalAddress()+":"+destSocket.getLocalPort(),destSocketIn,clientSocketOut);
					destToClientConnection.start();
				}
			}
			catch(IOException e){
				System.out.println(new Date()+" ERROR TCPForwarder: Something went wrong!\n"+e.getMessage());
				terminate();
			}
		}
		
		if (serverSocket != null){
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(new Date()+" INFO TCPForwarder: Server socket closed!");
		}
	}
	
	public void terminate(){
		running = false;
	}
	
	/**
	 * Inner class: Forwarder
	 * 
	 * Does the actual forwarding of the streams between client and destination.
	 *
	 */
	class Forwarder extends Thread {
		// max packet size is 65535 bytes
		private static final int BUFFER_SIZE = 65535;
		private DataInputStream socketIn; //reader
		private DataOutputStream socketOut; //writer
		private volatile boolean running = true;
		private String source;
	    
		public Forwarder(String source, DataInputStream socketIn, DataOutputStream socketOut){
			this.socketIn = socketIn;
			this.socketOut = socketOut;
			this.source = source;
			//System.out.println(new Date()+" INFO TCPForwarder: In/Output streams connected...");
		}

		public void run() {
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = 0;
			while(running) {
				try {
					// Reading stream
					//System.out.println(new Date()+" INFO TCPForwarder: Listening for reading...");
					bytesRead = socketIn.read(buffer);
					System.out.println(new Date()+" INFO TCPForwarder: Received " +
							bytesRead + " bytes" +
							" from " + source);
					if (bytesRead==-1){
						//System.out.println(new Date()+" INFO TCPForwarder: Nothing more to read...");
						this.terminate();
					} else {
						// Writing stream
						//System.out.println(new Date()+" INFO TCPForwarder: Listening for writing...");
						socketOut.write(buffer, 0, bytesRead);
						//System.out.println(new Date()+" INFO TCPForwarder: Writing...");
						socketOut.flush();
					}
				} catch (IOException e){
					System.out.println(new Date()+" INFO TCPForwarder: Read/Write stopped!\n" + e.getMessage());
					this.terminate();
				}
				
				yield(); //Gives other threads the chance to do something
						
				try {
					//System.out.println(new Date()+" INFO TCPForwarder: Sleeping...");
					Thread.sleep(1000); //Limits the number of loops per time unit for relief of the CPU
				} catch (InterruptedException e) {
					System.out.println(new Date()+" ERROR TCPForwarder: Sleeping failed!" + e.getMessage());
				}
			}
		}

	    public void terminate() {
	        running = false;
	    }
	    
	}
}
