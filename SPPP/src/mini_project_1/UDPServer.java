package mini_project_1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Date;

public class UDPServer extends Thread{
	
	public static void main(String[] args) throws IOException {
		// Parameter check
		if (args.length < 1) {
			System.out.println (new Date()+" ERROR UDPServer: Usage: UDPServer <Port>");
			System.exit(-1);
		}
		
		// Input Port
		int serverPort = Integer.parseInt(args[0]);
		
		// Start UDPServer
		new UDPServer(serverPort);
	}

	// Default number of milliseconds between termination checks   
	//public static int DEFAULT_TERMINATION_CHECK_MILLIS = 5000;   
	   
	// Maximum UDP data length possible (in bytes)   
	public static int MAX_LENGTH = 65508;   
	   
	// UDP socket used to receive echo requests   
	protected DatagramSocket socket;   
	     
	// Controls execution of the datagram receive thread   
	protected boolean running = true;  
	  
	public UDPServer(int port) throws SocketException{
		// create socket at agreed port
		socket = new DatagramSocket(port);
		//socket.setSoTimeout(DEFAULT_TERMINATION_CHECK_MILLIS);
		System.out.println(new Date()+" INFO UDPServer: Listening at " + socket.getLocalAddress() + ":" + socket.getLocalPort()+"\n");
		start();
	}
	
	public void run() { 
		byte[] buffer = new byte[MAX_LENGTH];
		DatagramPacket request = new DatagramPacket(buffer, buffer.length);
		
		while(running){
			try{	
				System.out.println (new Date()+" INFO UDPServer: Listening...\n");
				//request.setLength(MAX_LENGTH);
				socket.receive(request);   
				System.out.println (new Date()+" INFO UDPServer:\n--------------------UDP--------------------\n");
				System.out.println(new Date()+" INFO UDPServer: Received " +
						request.getLength() + " bytes" +
						" from " + 
						request.getAddress().toString() +
						":"  + request.getPort());
				DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(), request.getPort());
				System.out.println (new Date()+" INFO UDPServer: Writing...");
				socket.send(reply);
			}
			catch (SocketException e){
				System.out.println(new Date()+" UDPServer: Socket: " + e.getMessage());
			}
			catch (IOException e) {
				System.out.println(new Date()+" UDPServer: IO: " + e.getMessage());
			}
		}
		
		if(socket != null)
			socket.close();
		
	}
	
	public void terminate(){
		running = false;
	}
}
