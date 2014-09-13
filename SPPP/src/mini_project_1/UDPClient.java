package mini_project_1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;

public class UDPClient {

	public static void main(String args[]){ 
		
		// Parameter check
		if (args.length < 2) {
			System.out.println (new Date()+" ERROR TCPClient: Usage: UDPClient <Host> <Port> <Message>");
			System.exit(-1);
		}
		
		// Input Host
		InetAddress host = null;
		try {
			host = InetAddress.getByName(args[0]);
		} catch (UnknownHostException e) {
			System.out.println(new Date()+" ERROR UDPClient: " + e.getMessage());
			System.exit(-1);
		}
		
		// Input Port
		int serverPort = Integer.parseInt(args[1]);
		
		// Input Message
		String message = args[2];
		byte [] writeBuffer = args[2].getBytes();
		
		DatagramSocket socket = null;
		try {
			//  Build up UDP Connection and send request 
			socket = new DatagramSocket();
			DatagramPacket request = new DatagramPacket(writeBuffer, message.length(), host, serverPort);
			socket.send(request);	
			
			// Receive reply
			byte[] readBuffer = new byte[message.length()];
			DatagramPacket reply = new DatagramPacket(readBuffer, readBuffer.length);	
			socket.receive(reply);
			System.out.println(new Date()+" INFO UDPClient: Reply: " + new String(reply.getData()));	
			
		}
		catch (SocketException e){
			System.out.println(new Date()+" ERROR UDPClient: Socket: " + e.getMessage());
		}
		catch (IOException e){
			System.out.println(new Date()+" ERROR UDPClient: IO: " + e.getMessage());
		}
		finally {
			// Closing UDP Connection
			if(socket != null) 
				socket.close();
		}
	}
}
