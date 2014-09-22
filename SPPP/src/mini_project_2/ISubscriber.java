package mini_project_2;

public interface ISubscriber {
	void subscribe();

	void unsubscribe();

	void notifyMessage(String message);
}
