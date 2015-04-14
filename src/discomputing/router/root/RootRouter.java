package discomputing.router.root;
import java.io.IOException;

import discomputing.router.Router;

public class RootRouter extends Router {
	public RootRouter(int portNumber) {
		super(portNumber);
	}

	@Override
	public void listen() throws IOException {
		while(running == true){
			connectionSocket = serverSocket.accept();
			rThread = new RootRouterThread(connectionSocket);
			rThread.start();
		}
		closeConnections();
	}

	@Override
	public void closeConnections() {
		// TODO Auto-generated method stub
		
	}

}
