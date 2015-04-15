package discomputing.router;

/* Abstract Class: Router
 * 
 * Description: A Router creates a serverSocket on a given port in order to listen for connections
 * 				If the port is not available, the Router will not be created.
 * 				An implementation of a Router should be able to listen for connections and close connections
 */	
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
