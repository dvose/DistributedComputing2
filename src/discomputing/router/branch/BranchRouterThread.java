package discomputing.router.branch;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import discomputing.helper.PacketParser;
import discomputing.router.RouterThread;

public class BranchRouterThread extends RouterThread {
	static HashMap<String,String[]> peerTable = new HashMap<String, String[]>();
	Socket rootSocket = null;
	BufferedReader rootMessageIn = null;
	PrintWriter rootMessageOut = null;
	String address = null;
	int port = 0;
	String source = null;

	public BranchRouterThread(Socket connectionSocket, Socket rootSocket,BufferedReader rootMessageIn, PrintWriter rootMessageOut, String address, int port) throws IOException{
		super(connectionSocket);
		this.address = address;
		this.port = port;
		this.rootSocket = rootSocket;
		this.rootMessageIn = rootMessageIn;
		this.rootMessageOut = rootMessageOut;
	}
	
	public void run(){
		try {
			boolean connected = true;
			while(connected){
				packet = messageIn.readLine();
				if(packet == null){
					if(source.equals("Peer")){
						peerTable.remove(peerName);
						super.printPeerTable(peerTable);
					}
					break;
				}
				parsedPacket = PacketParser.parse(packet);
				
				//handle different packet types
				String type = parsedPacket.get("Type");
				source = parsedPacket.get("Source");
				
				//initial peer-router handshake, add peer to peerTable
				if(type.equals("PeerHandshake")){
					super.peerHandShake(peerTable);
				}
				
				//requesting peer by name
				else if(type.equals("PeerRequest")){
					boolean foundPeer = false;
					
					//if request was sent from root router, don't bother asking root router about the peer
					if(source.equals("RootRouter")){
					 	foundPeer= super.peerRequest(peerTable, parsedPacket.get("PeerName"));
					 	if(!foundPeer){
					 		messageOut.println("Type:PeerLookup|Message:Failed");
					 	}
					}
					
					//if request was sent from peer, forward request to root router if peer not found 
					else if(source.equals("Peer")){
						foundPeer = super.peerRequest(peerTable, parsedPacket.get("PeerName"));
						
						//if peer is not found, ask root to find the peer
						if(!foundPeer){
							foundPeer = forwardToRootRouter(parsedPacket);
							//if no router found peer
							if(!foundPeer){
								messageOut.println("Type:PeerLookup|Message:Failed");
							}	
						}
					}
				}
				
				//root router requesting peer by name
				else if(type.equals("RootPeerRequest")){
					String[] peerAddress = peerTable.get((parsedPacket.get("PeerName")));
					if(peerAddress != null)
						messageOut.println("Type:PeerLookup|Message:Success|Address:" + peerAddress[0] + "|Port:" + peerAddress[1]);
					else
						messageOut.println("Type:PeerLookup|Message:Failed");
				}
					
				else if(type.equals("Disconnect")){
					//if peer is disconnecting, remove from peer table
					peerTable.remove(parsedPacket.get("Name"));
					super.printPeerTable(peerTable);
					messageOut.println("Disconnected");
					connected = false;
				}
				
			} 
		} catch (IOException e) {
			if(source.equals("Peer")){
				peerTable.remove(peerName);
				super.printPeerTable(peerTable);
			}
		}
	}

	private boolean forwardToRootRouter(HashMap<String, String> originalPacket) throws IOException {
		rootMessageOut.println("Type:PeerRequest|Source:BranchRouter|PeerName:" + originalPacket.get("PeerName")+"|Address:" + address + "|Port:" + port);
		packet = rootMessageIn.readLine();
		parsedPacket = PacketParser.parse(packet);
		if(parsedPacket.get("Message").equals("Success")){
			messageOut.println(packet);
			return true;
		}
		else
			return false;
	}
}
