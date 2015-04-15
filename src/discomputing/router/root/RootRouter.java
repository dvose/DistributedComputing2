package discomputing.router.root;
import java.io.IOException;

import discomputing.router.Router;

/* Class: RootRouter
 * Extends: Router
 * 
 * Description: RootRouter is a Router that Peers and BranchRouters can connect to.
 * 				The RootRouter is designed to be the first Router created in the p2p network.
 * 				Only 1 RootRoouter should be created per p2p network.
 *				Once the ServerSocket is connected, The RootRouter will spawn a RootRouterThread. 
 */	
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
