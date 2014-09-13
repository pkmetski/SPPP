package mini_project_1;

import java.util.Date;

public class RFC862 extends Thread {

	// http://read.pudn.com/downloads66/sourcecode/book/236908/%E3%80%8AJAVA%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B%E6%8C%87%E5%8D%97%E3%80%8B%E4%BB%A3%E7%A0%81/Ch10/com/wrox/EchoUDPServer.java__.htm
	private final int PORT = 7007; 
	
	public static void main(String[] args) throws InterruptedException{
		new RFC862();
	}

	public RFC862() {
		this.start();
	}

	public void run() {
		try {   
			System.out.println (new Date()+" INFO RFC862: Starting UDP Service...");
			new UDPServer(PORT);
	    } catch (Throwable e) {   
	    	System.err.println(new Date()+" ERROR RFC862: UDP Service: " + e);   
	    	System.exit(1);   
	    }  
		
		try {   
			System.out.println (new Date()+" INFO RFC862: Starting TCP Service...");
			new TCPServer(PORT);
	    } catch (Throwable e) {   
	    	System.err.println(new Date()+" ERROR RFC862: TCP Service: " + e);   
	    	System.exit(1);   
	    }
		
	}

}
