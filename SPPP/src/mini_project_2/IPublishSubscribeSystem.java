package mini_project_2;

public interface IPublishSubscribeSystem {
	void initializePublisherListener();

	void initializeSubscriberListener();

	void notifySubscribers();
}
