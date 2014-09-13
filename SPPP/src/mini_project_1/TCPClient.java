package mini_project_1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Date;

public class TCPClient {
	
	public static void main (String args[]) throws Exception 
	{
		// Parameter check
		if (args.length < 3) {
			System.out.println (new Date()+" ERROR TCPClient: Usage: TCPClient <Host> <Port> <Message>");
			System.exit(-1);
		}
		
		// Input Host
		String host = args[0];
		
		// Input Port (e.g. 7896)
		int serverPort = Integer.parseInt(args[1]);
		
		// Input Message
		String message = args[2];
		
		// Build up TCP Connection with an input and an output stream
		Socket socket = new Socket(host, serverPort);  
		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		
		// Send request
		out.writeUTF(message);      	// UTF is a string encoding see Sec. 4.4
		
		// Receive reply
		String data = in.readUTF();	    // read a line of data from the stream
		System.out.println(new Date()+" INFO TCPClient: Received: "+ data);
		
		// Closing TCP Connection
		socket.close();
     }

}
