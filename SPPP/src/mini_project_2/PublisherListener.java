package mini_project_2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * PublisherListener
 *
 */
public class PublisherListener extends Thread{
	
	protected ServerSocket serverSocket = null;
	protected Socket publisherSocket = null;
	protected PublishSubscribeSystem publishSubscribeSystem = null;
	protected boolean running = true;
	
	public PublisherListener(int serverPort, PublishSubscribeSystem publishSubscribeSystem){
		try {
			serverSocket = new ServerSocket(serverPort);
			System.out.println("Created PublisherListener for port "+serverPort+"...\n");
		} catch(IOException e){
			System.out.println("Could not create the server port!\n" + e.getMessage());
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
				// Waiting for a publisher to connect
				publisherSocket = serverSocket.accept();
				Client client = publishSubscribeSystem.addPublisher(publisherSocket);

				// For each publisher there is a publisher channel
				new PublisherChannel(client, publishSubscribeSystem);
			}
			catch(IOException e){
				System.out.println("Something went wrong!\n"+e.getStackTrace());
				terminate();
			} catch (Exception e) {
				e.printStackTrace();
				terminate();
			}
		}
		
		if (serverSocket != null){
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("PublisherListener closed!");
		}
	}
	
	public void terminate(){
		running = false;
	}
	
	/**
	 * Inner Class: PublisherChannel
	 * 
	 * For each publisher there is a publisher channel.
	 * The input stream, which is the publisher stream, is read and written to the output streams for each subscriber.
	 *
	 */
	class PublisherChannel extends Thread {
		// Publisher TCP socket used to communicate 
		protected Client publisher;
		// Reader stream
		protected DataInputStream publisherIn;
		// Controls execution of the publisher receive thread   
		protected boolean running = true;
	    
		public PublisherChannel(Client publisher, PublishSubscribeSystem publishSubscribeSystem) throws IOException{
			this.publisher = publisher;
			try{
				this.publisherIn = new DataInputStream(publisher.getSocket().getInputStream());
			}
			catch (IOException e){
				System.out.println("Could not create publisher channel !\n" + e.getMessage());
				throw e;
			}
			start();
		}

		public void run() {
			while(running) {
				try{
					// Reading publisher stream
					String message = publisherIn.readUTF();
					System.out.println("\tReceived message '" + message + "' from " + publisher.getName());
					Set<Client> subscribers = publishSubscribeSystem.getSubscribers();
					Set<Client> removeSubscriberSockets = new HashSet<Client>();
					// Writing into the streams of each subsciber
					for (Client subscriber : subscribers){
						try {
							DataOutputStream subscriberOut = new DataOutputStream(subscriber.getSocket().getOutputStream());
							// Writing stream
							subscriberOut.writeUTF(message);
							subscriberOut.flush();
							System.out.println("\tSent message '" + message + "' to " + subscriber.getName());
						}
						catch (IOException ex){
							removeSubscriberSockets.add(subscriber);
						}
					}
					// Delete subscibers which cannot be reached
					for (Client subscriber : removeSubscriberSockets){
						publishSubscribeSystem.removeSubscriber(subscriber);
					}
				} catch (IOException e){
					publishSubscribeSystem.removePublisher(publisher);
					terminate();
				} 
			}
		}

	    public void terminate() {
	    	System.out.println("\tStop PublisherChannel for " + publisher.getName());
	        running = false;
	    }
	    
	} // Inner Class: PublisherChannel
	
}