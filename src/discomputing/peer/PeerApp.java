package discomputing.peer;

import java.io.IOException;
import java.util.Scanner;

public class PeerApp {
	static Scanner in = new Scanner(System.in);
	static Peer peer = null;

	public static void main(String[] args) {
		
		
		System.out.print("Peer Application\n-----------------------\nPeer Name: ");
		String name = in.nextLine();
		System.out.print("Listening Port: ");
		int port = in.nextInt();
		in.nextLine();
		System.out.println("-----------------------");		
		try{
			peer = new Peer(name, port);
		}
		catch(Exception e){
			System.err.println("Unable to create peer");
			System.exit(-1);
		}
		
		System.out.println("Peer address to connect: ");
		String peerAddress = in.nextLine();
		System.out.println("Peer port: ");
		int peerPort = in.nextInt();
		
		try {
			peer.connectToPeer(peerAddress, peerPort);
			System.out.println(peer.p2pReadString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(peer.isConnectedToPeer())
		{
			try {
				p2pOptions();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/*in.reset();
		System.out.print("SETUP\n-----------------------\nAddress of p2p Router: \n");
		String routerAddress = in.nextLine();
		System.out.print("p2p Router Port Number: ");
		int routerPort = in.nextInt();
		
		try{
			peer.connectToRouter(routerAddress, routerPort);
		}
		catch(Exception e){
			System.err.println("Could not connect to p2p Router");
			System.exit(-1);
		}*/
	}
	public static void p2pOptions() throws IOException{
		int option = 0;
		System.out.println("P2P Options\n"
						 + "--------------------------------\n"
						 + "1)Send File\n"
						 + "2)Receieve File\n"
						 + "3)Disconnect from Peer\n");
		option = in.nextInt();
		peer.p2pSendInt(option);
		if(option == 1){
			peer.sendFile();
		}
		else if(option == 2){
			System.out.println("List of Available Files");
			int numFiles = peer.p2pReadInt();
			for(int i = 0; i < numFiles; i++){
				System.out.println(peer.p2pReadString());
			}
			System.out.println("File name: ");
			in.nextLine();
			try {
				peer.receiveFile(in.nextLine());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(option == 3){
			peer.closePeerConnection();
			System.out.println("Disconnected from Peer");
		}
	}	
}
