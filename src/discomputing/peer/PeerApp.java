package discomputing.peer;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class PeerApp {
	static Scanner in = new Scanner(System.in);
	static Peer peer = null;

	public static void main(String[] args) {
		boolean running = true;
		int port = 0;
		
		System.out.print("PEER SETUP\n-----------------------\nPeer Name: ");
		String name = in.nextLine();
		System.out.print("Listening Port: ");
		try{
			port = in.nextInt();
		}
		catch(InputMismatchException e){
			System.err.println("Port must be an integer");
			System.exit(-1);
		}
		in.nextLine();
		System.out.println("-----------------------");		
		try{
			peer = new Peer(name, port);
		}
		catch(Exception e){
			System.err.println("Unable to create peer");
			System.exit(-1);
		}
		
		System.out.println("ROUTER CONNECTION\n\n-----------------------\nRouter Address: ");
		String routerAddress = in.nextLine();
		
		System.out.println("Router port: ");
		int routerPort = 0;
		try{
			routerPort = in.nextInt();
		}
		catch(InputMismatchException e){
			System.err.println("Port must be an integer");
			System.exit(-1);
		}
		
		try {
			peer.connectToRouter(routerAddress, routerPort);
		} catch (IOException e1) {
			System.err.println("Unable to connect to router");
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
				in.nextLine();
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
				System.out.println("Router Address: " + peer.getRouterAddress() + "\nRouter Port: " + peer.getRouterPort());
				break;
			case 4:
				System.out.println("Peer is exiting. Goodbye!");
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
		System.out.println("Peer Name: ");
		String peerName = in.nextLine();
		
		try {
			peer.connectToPeer(peerName);
			if(peer.isConnectedToPeer())
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
				System.err.println("Error Unable to connect");
				e.printStackTrace();
			}
		}
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
		try{
			option = in.nextInt();
		}
		catch(InputMismatchException e){
			in.nextLine();
			System.out.println("Please select a valid option");
			return;
		}
		peer.p2pSendInt(option);
		if(option == 1){
			in.nextLine();
			System.out.println("File name: ");
			String fileName = in.nextLine();
			
			peer.sendFile(fileName);
			System.out.println("File Sent");
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
