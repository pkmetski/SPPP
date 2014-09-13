package mini_project_1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * TCPServer
 *
 */
public class TCPServer {
	
	public static void main(String[] args) throws IOException {
		// Parameter check
		if (args.length < 1) {
			System.out.println (new Date()+" ERROR TCPServer: Usage: TCPServer <Port>");
			System.exit(-1);
		}
		
		// Input Port
		int serverPort = Integer.parseInt(args[0]);
		
		// Start TCPServer
		new TCPServer(serverPort);
	}
	
	public TCPServer(int serverPort){
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(serverPort);
			System.out.println(new Date()+" INFO TCPServer: Created server for port "+serverPort+"...\n");
		} catch(IOException e){
			System.out.println(new Date()+" ERROR TCPServer: Could not create the server port!\n" + e.getMessage());
			return;
		}

		// Because serverSocket.accept is a blocking call, which will be only ended if a client is connecting to 
		// the server, this call is wrapped into a thread.
		new ServerAccept(serverSocket);
	}
	
	/**
	 * Inner class: ServerAccept
	 * 
	 * Because serverSocket.accept is a blocking call, which will be only ended if a client is connecting to the server,
	 * this call is wrapped into a thread.
	 *
	 */
	class ServerAccept extends Thread {
		
		protected ServerSocket serverSocket = null;
		protected Socket clientSocket = null;
		protected boolean running = true;
		
		public ServerAccept(ServerSocket serverSocket){
			this.serverSocket = serverSocket;
			start();
		}
		
		public void run(){
			while(running){
				try{
					// Waiting for a client to connect
					clientSocket = serverSocket.accept();
					System.out.println (new Date()+" INFO TCPServer:\n--------------------TCP--------------------\n");
					System.out.println(new Date()+" INFO TCPServer: Client made a connection...");

					// Because the server should be able to receive several client requests, the TCP connection
					// to clients is handled in a thread.
					new TCPConnection(clientSocket);
				}
				catch(IOException e){
					System.out.println(new Date()+" ERROR TCPServer: Something went wrong!");
					terminate();
				}
			}
			
			if (serverSocket != null){
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println(new Date()+" INFO TCPServer: Server socket closed!");
			}
		}
		
		public void terminate(){
			running = false;
		}
		
		/**
		 * Inner Class: TCP Connection
		 * 
		 * Because the server should be able to receive several client requests, the TCO connection to clients 
		 * is handled in a thread.
		 *
		 */
		class TCPConnection extends Thread {
			// Max packet size is 65535 bytes
			private static final int BUFFER_SIZE = 65535;
			// Client TCP socket used to communicate 
			protected Socket clientSocket;
			// Reader stream
			protected DataInputStream socketIn;
			// Writer stream
			protected DataOutputStream socketOut;
			// Controls execution of the datagram receive thread   
			protected boolean running = true;
		    
			public TCPConnection(Socket clientSocket) throws IOException{
				//System.out.println(new Date()+" INFO TCPServer: Start TCP connection...");
				this.clientSocket = clientSocket;
				try{
					// Connect input/output streams
					this.socketIn = new DataInputStream(clientSocket.getInputStream());
					this.socketOut = new DataOutputStream(clientSocket.getOutputStream());
					//System.out.println(new Date()+" INFO TCPServer: In/Output streams connected...");
				}
				catch (IOException e){
					System.out.println(new Date()+" ERROR TCPServer: Could not connect input/output streams!\n" + e.getMessage());
					throw e;
				}
				start();
			}

			public void run() {
				byte[] buffer = new byte[BUFFER_SIZE];
				int bytesRead = 0;
				while(running) {
					try{
						// Reading stream
						System.out.println(new Date()+" INFO TCPServer: Listening for reading...");
						bytesRead = socketIn.read(buffer);
						System.out.println(new Date()+" INFO TCPServer: Received " +
								bytesRead + " bytes" +
								" from " + 
								clientSocket.getLocalAddress() +
								":"  + clientSocket.getLocalPort());
						if (bytesRead==-1){
							System.out.println(new Date()+" INFO TCPServer: Nothing more to read...");
							terminate();
						}
						else {
							// Writing stream
							//System.out.println(new Date()+" INFO TCPServer: Listening for writing...");
							socketOut.write(buffer, 0, bytesRead);
							System.out.println(new Date()+" INFO TCPServer: Writing...");
							socketOut.flush();
						}
						
						yield(); //Gives other threads the chance to do something
						
						try {
							//System.out.println(new Date()+" INFO TCPServer: Sleeping...");
							Thread.sleep(1000); //Limits the number of loops per time unit for relief of the CPU
						} catch (InterruptedException e) {
							System.out.println(new Date()+" ERROR TCPServer: Sleeping failed!" + e.getMessage());
						}
					} catch (IOException e){
						System.out.println(new Date()+" ERROR TCPServer: Read/Write error!\n" + e.getMessage());
						terminate();
					} 
				}
				
				if (clientSocket != null){
					try {
						clientSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println(new Date()+" INFO TCPServer: Client socket closed!");
				}
				System.out.println(new Date()+" INFO TCPServer: Stop TCP Connection.");
			}

		    public void terminate() {
		        running = false;
		    }
		    
		} // Inner Class: TCP Connection
		
	} //Inner class: ServerAccept
	
}
