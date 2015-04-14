package discomputing.router;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

public class RouterThread extends Thread {
	protected BufferedReader messageIn = null;
	protected PrintWriter messageOut = null;
	protected Socket connectionSocket = null;
	protected String packet = null;
	protected HashMap<String, String> parsedPacket = null;
	protected String peerName = null;
	
	public RouterThread(Socket connectionSocket) throws IOException{
		this.connectionSocket = connectionSocket;
		messageIn = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		messageOut = new PrintWriter(connectionSocket.getOutputStream(),true);
	}

	public void peerHandShake(HashMap<String, String[]> peerTable) {
		String[] peerAddress = {parsedPacket.get("Address"), parsedPacket.get("Port")};
		peerName = parsedPacket.get("Name");
		peerTable.put(peerName, peerAddress );
		printPeerTable(peerTable);
		messageOut.println("Type:Confirm|Message:Peer Added to list");
	}
	
	public boolean peerRequest(HashMap<String, String[]> peerTable, String peerName){
		String[] peerAddress = peerTable.get(peerName);			
		if(peerAddress != null){
			messageOut.println("Type:PeerLookup|Message:Success|Address:" + peerAddress[0] + "|Port:" + peerAddress[1]);
			return true;
		}
		else{
			return false;
		}
	}
	public void printPeerTable(HashMap<String, String[]> peerTable){
		System.out.println("\nPEER TABLE\n--------------------------------------------------");
		Set<Entry<String, String[]>> entries = peerTable.entrySet();
		for (Entry<String, String[]> entry : entries) {
		    String name = entry.getKey();
		    String address = entry.getValue()[0];
		    String port = entry.getValue()[1];
		    System.out.println("|Name: " + name  + " | Address: " + address + " | Port: " + port + " |");
		    System.out.println("--------------------------------------------------");
		}
	}
}
