package discomputing.router;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class Router {
	protected Socket connectionSocket = null;
	protected ServerSocket serverSocket = null;
	protected RouterThread rThread = null;
	protected boolean running = true;
	int portNumber = 0;
	
	public Router(int portNumber){
		this.portNumber = portNumber;
		try{
			serverSocket = new ServerSocket(this.portNumber);
		}
		catch(Exception e){
			System.err.println("Unable to listen of port " + this.portNumber);
			System.exit(-1);
		}
	}
	
	public abstract void listen() throws IOException;
	public abstract void closeConnections();
}
