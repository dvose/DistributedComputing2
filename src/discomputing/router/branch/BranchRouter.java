package discomputing.router.branch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

import discomputing.helper.PacketParser;
import discomputing.router.Router;

/* Class: BranchRouter
 * Extends: Router
 * 
 * Description: BranchRouter is a Router that Peers can connect to.
 * 				A BranchRouter must connect to a RootRouter.
 * 				The RootRouter must be created first before creating a BranchRouter.
 *				Once the ServerSocket is connected, a BranchRouter will spawn a BranchRouterThread. 
 */				
public class BranchRouter extends Router {
	Socket rootSocket = null;
	BufferedReader rootMessageIn = null;
	PrintWriter rootMessageOut = null;
	String Address = InetAddress.getLocalHost().getHostAddress();
	int port = 0;
	
	public BranchRouter(int portNumber, String rootAddress, int rootPort) throws Exception {		
		super(portNumber);
		this.port = portNumber;
		
		//tries to connect to RootRouter
		try {
			rootSocket = new Socket(rootAddress, rootPort);
			rootMessageIn = new BufferedReader(new InputStreamReader(rootSocket.getInputStream()));
			rootMessageOut = new PrintWriter(rootSocket.getOutputStream(),true);
		} catch (Exception e){
			System.err.println("Unable to connect to the Root Router");
			System.exit(-1);
		}
		//initial handshake with Root Router
		String outPacket = "Type:RouterHandshake|Source:BranchRouter|Address:" + InetAddress.getLocalHost().getHostAddress()+"|Port:"+portNumber;
		rootMessageOut.println(outPacket);
		HashMap<String,String> inPacket = PacketParser.parse(rootMessageIn.readLine()); 
		System.out.println("Root Router: " + inPacket.get("Message"));
	}

	@Override
	public void listen() throws IOException {
		while(running == true){
			connectionSocket = serverSocket.accept();
			rThread = new BranchRouterThread(connectionSocket, rootSocket, rootMessageIn, rootMessageOut, Address, port);
			rThread.start();
		}
		closeConnections();
	}

	@Override
	public void closeConnections() {
		try {
			rootSocket.close();
			connectionSocket.close();
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
