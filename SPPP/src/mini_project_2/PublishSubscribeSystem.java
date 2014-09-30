package mini_project_2;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * PublishSubscribeSystem
 * 
 * Starts the listeners and holds the data.
 *
 */
public class PublishSubscribeSystem extends Thread {
	
	protected PublisherListener publisherListener;
	protected SubscriberListener subscriberListener;
	protected int publisherListenerPort = 0;
	protected int subscriberListenerPort = 0;
	protected Set<Client> publishers = new HashSet<Client>();
	protected Set<Client> subscribers = new HashSet<Client>();
	protected int publisherCounter = 1;
	protected int subscriberCounter = 1;
	protected boolean running = true;
	
	/**
	 * Main
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new PublishSubscribeSystem(Configuration.PUBLISH_SUBSCRIBE_SYSTEM_PUBLISHER_PORT, Configuration.PUBLISH_SUBSCRIBE_SYSTEM_SUBSCRIBER_PORT);
	}
	
	/**
	 * Starts the PublishSubscribeSystem
	 * @param publisherListenerPort
	 * @param subscriberListenerPort
	 */
	public PublishSubscribeSystem(int publisherListenerPort, int subscriberListenerPort){
		this.publisherListenerPort = publisherListenerPort;
		this.subscriberListenerPort = subscriberListenerPort;
		
		subscriberListener = new SubscriberListener(this.subscriberListenerPort,this);
		publisherListener = new PublisherListener(this.publisherListenerPort,this);
		
		start();
	}
	
	/**
	 * Run
	 */
	public void run(){
		while(running){
			// keep on running...
		}
	}
	
	/**
	 * Terminate
	 */
	public void terminate(){
		
	}
	
	
	/**
	 * Adds a subscriber to the subscriber map
	 * @param s
	 * @throws Exception 
	 */
	synchronized public Client addSubscriber(Socket s) throws Exception{
		Client subscriber = new Client(s);
		if (!subscribers.contains(subscriber)){
			String subscriberName = "B"+subscriberCounter++;
			System.out.println("Sink process '"+ subscriberName +"' starts");
			subscriber.setName(subscriberName);
			subscribers.add(subscriber);
			return subscriber;
		}
		for (Client existingSubscriber : subscribers){
			if (existingSubscriber.equals(subscriber))
				return existingSubscriber;
		}
		throw new Exception("Could not add subscriber");
	}
	
	/**
	 * Removes a subscriber from the subscriber map
	 * @param c
	 */
	synchronized public void removeSubscriber(Client subscriber){
		if (subscribers.contains(subscriber)){
			subscribers.remove(subscriber);
			subscriber.close();
			System.out.println("Subscriber "+subscriber.getName()+" disconnected");
		}
	}
	
	/**
	 * Returns the subscriber map
	 * @return
	 */
	public Set<Client> getSubscribers(){
		return subscribers;
	}
	
	/**
	 * Adds a publisher to the publisher map
	 * @param s
	 * @return
	 * @throws Exception
	 */
	synchronized public Client addPublisher(Socket s) throws Exception{
		Client publisher = new Client(s);
		if (!publishers.contains(publisher)){
			String publisherName = "A"+publisherCounter++;
			System.out.println("Source process '"+ publisherName +"' starts");
			publisher.setName(publisherName);
			publishers.add(publisher); 
			return publisher;
		}
		for (Client existingPublisher : publishers){
			if (existingPublisher.equals(publisher))
				return existingPublisher;
		}
		throw new Exception("Could not add publisher");
	}
	
	/**
	 * Removes a publisher from the publisher map
	 * @param c
	 */
	synchronized public void removePublisher(Client publisher){
		if (publishers.contains(publisher)){
			publisher.close();
			System.out.println("Publisher "+publisher.getName()+" disconnected");
			publishers.remove(publisher);
		}
	}
}
