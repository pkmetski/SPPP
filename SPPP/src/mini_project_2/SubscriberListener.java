package mini_project_2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * SubscriberListener
 *
 */
public class SubscriberListener extends Thread{
	
	protected ServerSocket serverSocket = null;
	protected Socket subscriberSocket = null;
	protected PublishSubscribeSystem publishSubscribeSystem = null;
	protected boolean running = true;
	
	public SubscriberListener(int serverPort, PublishSubscribeSystem publishSubscribeSystem){
		try {
			serverSocket = new ServerSocket(serverPort);
			System.out.println("Created SubscriberListener for port "+serverPort+"...\n");
		} catch(IOException e){
			System.out.println("Could not create the SubscriberListener port!\n" + e.getMessage());
			return;
		}
		
		this.publishSubscribeSystem = publishSubscribeSystem;
		
		// Because serverSocket.accept is a blocking call, which will be only ended if a client is connecting to 
		// the server, this call is wrapped into a thread.
		start();
	}
	
	public void run(){
		while(running){
			try{
				// Waiting for a subscriber to connect
				subscriberSocket = serverSocket.accept();
				publishSubscribeSystem.addSubscriber(subscriberSocket);
			} catch (Exception e) {
				System.out.println("Something went wrong!\n"+e.getStackTrace());
				terminate();
			}
		}
		
		if (serverSocket != null){
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("SubscriberListener closed!");
		}
	}
	
	public void terminate(){
		running = false;
	}
	
}