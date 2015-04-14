package discomputing.router.root;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import discomputing.helper.PacketParser;
import discomputing.router.RouterThread;

public class RootRouterThread extends RouterThread {
	static ArrayList<String[]> routerList = new ArrayList<String[]>();
	static HashMap<String, String[]> peerTable = new HashMap<String, String[]>();
	String source = null;
	String branchName = null;
	String[] branchAddress = null;
	
	public RootRouterThread(Socket connectionSocket) throws IOException{
		super(connectionSocket);
	}
	
	public void run(){
		try{
			boolean connected = true;
			while(connected){
				packet = messageIn.readLine();
				if(packet == null){
					if(source.equals("BranchRouter")){
						routerList.remove(branchAddress);
						printRouterList();
					}
					else if(source.equals("Peer")){
						peerTable.remove(peerName);
						super.printPeerTable(peerTable);
					}
					break;
				}
				parsedPacket = PacketParser.parse(packet);
				
				//handle different packet types
				String type = parsedPacket.get("Type");
				source = parsedPacket.get("Source");
				
				//initial branch router - root router handshake, add router to routerList
				if(type.equals("RouterHandshake")){
					String routerAddress[] = {parsedPacket.get("Address"), parsedPacket.get("Port")};
					routerList.add(routerAddress);
					printRouterList();
					branchAddress = routerAddress;
					System.out.println("Branch Router Added\nAddress:" + routerAddress[0] + "\nPort: " + routerAddress[1]);
					messageOut.println("Type:Confirm|Source:RootRouter|Message:Router Added to list");
				}
				
				//initial peer-router handshake, add peer to peerTable
				if(type.equals("PeerHandshake")){
					super.peerHandShake(peerTable);
				}
				
				//peer requesting another peer by name
				else if(type.equals("PeerRequest")){
					boolean foundPeer = super.peerRequest(peerTable, parsedPacket.get("PeerName"));
					//if peer is not found, search in branch routers
					if(!foundPeer){
						//if source is Peer, look in every branch router
						if(source.equals("Peer")){
							foundPeer = forwardToBranches(parsedPacket,null, null);
						}
						//if source is BranchRouter, look in every branch router except that branch router
						else if(source.equals("BranchRouter")){
							foundPeer = forwardToBranches(parsedPacket,parsedPacket.get("Address"), parsedPacket.get("Port"));
						}
						
						//foundPeer = searchBranchRouters(parsedPacket)
						//if no router found peer
						if(!foundPeer){
							messageOut.println("Type:PeerLookup|Message:Failed");
						
						}
					}
				}
				
				//disconnecting from root router
				else if(type.equals("Disconnect")){
					//if branch router is disconnecting, remove from router list
					if(source.equals("BranchRouter")){
						String routerAddress[] = {parsedPacket.get("Address"), parsedPacket.get("Port")};
						routerList.remove(routerAddress);
						printRouterList();
						messageOut.println("Disconnected");
					}
					
					//if peer is disconnecting, remove from peer table
					else{
						peerTable.remove(parsedPacket.get("Name"));
						super.printPeerTable(peerTable);
						messageOut.println("Disconnected");
					}
					connected = false;
				}
			} 
		}
		catch (IOException e) {
			System.err.println("Thread interupted");
		}
	}

	private void printRouterList() {
		System.out.println("\nROUTER LIST\n-------------------------------------------");
		for (int i=0; i<routerList.size(); i++) {
		    System.out.println("| Address: " + routerList.get(i)[0] + " | Port: " + routerList.get(i)[1] + " |");
		    System.out.println("-------------------------------------------");
		}
	}

	private boolean forwardToBranches(HashMap<String, String> originalPacket, String excludeBranch, String excludePort) throws NumberFormatException, UnknownHostException, IOException {
		String request = "Type:PeerRequest|Source:RootRouter|PeerName:"+originalPacket.get("PeerName");	
		for(int i=0; i<routerList.size(); i++){
			
			//exclude original branch router from search
			if(routerList.get(i)[0].equals(excludeBranch) && routerList.get(i)[1].equals(excludePort)){
				continue;
			}
			
			Socket branchRouter = new Socket(routerList.get(i)[0], Integer.parseInt(routerList.get(i)[1]));
			BufferedReader branchIn = new BufferedReader(new InputStreamReader(branchRouter.getInputStream()));
			PrintWriter branchOut = new PrintWriter(branchRouter.getOutputStream(), true);
			
			branchOut.println(request);
			packet = branchIn.readLine();
			parsedPacket = PacketParser.parse(packet);
			
			if(parsedPacket.get("Message").equals("Success")){
				//forward Peer address to peer
				messageOut.println(packet);
				
				//close connections
				branchOut.print("Type:Disconnect");
				branchRouter.close();
				branchIn.close();
				branchOut.close();
				
				return true;
			}
			
			//close connections
			branchOut.print("Type:Disconnect");
			branchRouter.close();
			branchIn.close();
			branchOut.close();
		}
		return false;
	}
}
