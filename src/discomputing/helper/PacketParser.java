package discomputing.helper;

import java.util.HashMap;

public class PacketParser {
	public static HashMap<String, String> parse(String packet){
		HashMap<String, String> parsedPacket = new HashMap<String, String>();
		String[] splitedPacket = packet.split("\\|");
		for( String pack : splitedPacket){
			String[] packetValues = pack.split("\\:");
			parsedPacket.put(packetValues[0], packetValues[1]);
		}
		return parsedPacket;
	}
	
	/*public static void main(String args[]){
		String packet = "Name:Dustin|Address:192.168.1.1|Type:PeerHandshake";
		Map parsedPacket = parse(packet);
		Set<Entry> entries = parsedPacket.entrySet();
		for (Map.Entry<String, String> entry : entries) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    System.out.println(key + " " + value);
		}
	}*/
	
}
