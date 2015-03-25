package discomputing.peer;

import java.io.IOException;
import java.util.Scanner;

public class PeerApp {
	static Scanner in = new Scanner(System.in);
	static Peer peer = null;

	public static void main(String[] args) {
		boolean running = true;
		
		System.out.print("PEER SETUP\n-----------------------\nPeer Name: ");
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
		
		while(running){
			int option = 0;
			System.out.println(" ---------------------------------\n"
					  		  +"| PEER OPTIONS                 	  |\n"
					  		  +"|---------------------------------|\n"
					  		  +"| 1) Connect to a Peer            |\n"
					  		  +"| 2) View This Peer's Information |\n"
					  		  +"| 3) View Router Information      |\n"
					  		  +"| 4) Exit                         |\n"
					  		  +" ---------------------------------\n");
			try{
				option = in.nextInt();
			}
			catch(Exception e){
				System.out.println("Please select a valid option");
			}
			switch(option){
			case 1:
				connectP2P();
				break;
			case 2:
				System.out.println("Name: " + peer.getName()
								  +"\nAddress: " + peer.getAddress()
								  +"\nListening Port: " + peer.getListeningPort()
								  +"\n------------------------------------------\n");
				break;
			case 3:
				System.out.println("ROUTER INFO WILL GO HERE");
				break;
			case 4:
				System.exit(0);
				break;
			default:
				System.out.println("Please select a valid option");
				break;
			}
		}
	}	
	public static void connectP2P(){
		in.nextLine(); //reset scanner location
		System.out.println("Peer address to connect: ");
		String peerAddress = in.nextLine();
		System.out.println("Peer port: ");
		int peerPort = in.nextInt();
		
		try {
			peer.connectToPeer(peerAddress, peerPort);
			System.out.println(peer.p2pReadString());
		} catch (IOException e) {
			System.out.println("Peer unavailable");
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
		System.out.println(" -------------------------------\n"
						  +"| P2P Options                   |\n"
						  +"|-------------------------------|\n"
						  +"| 1) Send File                  |\n"
						  +"| 2) Receieve File              |\n"
						  +"| 3) Disconnect from Peer       |\n"
						  +" -------------------------------\n");
		option = in.nextInt();
		peer.p2pSendInt(option);
		if(option == 1){
			in.nextLine();
			System.out.println("File name: ");
			String fileName = in.nextLine();
			
			try{
				peer.sendFile(fileName);
				System.out.println("File Sent");
			}
			catch(IOException e){
				System.err.println("Unable to send file");
			}
		}
		else if(option == 2){
			System.out.println("List of Available Files");
			int numFiles = peer.p2pReadInt();
			System.out.println("numFiles: " + numFiles);
			for(int i = 0; i < numFiles; i++){
				System.out.println(peer.p2pReadString());
			}
			System.out.println("File name: ");
			in.nextLine();
			try {
				peer.receiveFile(in.nextLine());
				
			} catch (IOException e) {
				System.err.println("Unable to receive file");
			}
		}
		else if(option == 3){
			peer.closePeerConnection();
			System.out.println("Disconnected from Peer");
		}
	}	
}
