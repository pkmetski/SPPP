package mini_project_2;

import java.io.IOException;
import java.net.Socket;

public class Client {
	
	private String name;
	private Socket socket;
	
	public Client(Socket socket){
		this.socket = socket;
	}

	public int hashCode(){
		return this.socket.getLocalAddress().hashCode()+this.socket.getLocalPort();
	}
	
	public boolean equals(Object obj){
		if (obj.getClass().equals(this.getClass())){
			Client client = (Client) obj;
			if (this.socket.getInetAddress().equals(client.socket.getInetAddress()) && this.socket.getPort() == client.socket.getPort())
				return true;
		}
		return false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Socket getSocket() {
		return socket;
	}

	public void close() {
		if (socket != null){
			try {
				socket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}		
	}
}
