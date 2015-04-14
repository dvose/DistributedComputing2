package discomputing.peer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import discomputing.helper.PacketParser;


/* Class: Peer
 * Description: A Peer is a node that will create connections with other Peers through a Router.
 * 				
 */
public class Peer {
	private Socket routerSocket = null;
	private Socket peerSocket = null;
	private BufferedReader messageIn = null;
	private PrintWriter messageOut = null;
	private DataInputStream dataIn = null;
	private DataOutputStream dataOut = null;
	private String name = "";
	private int portNumber;
	private String routerAddress = "";
	private int routerPort = 0;
	String packet = null;
	HashMap<String, String> parsedPacket = null;
	
	public Peer(String name, int portNumber) throws IOException{
		this.portNumber = portNumber;
		ListeningThread lThread = new ListeningThread(this.portNumber);
		lThread.start();
		this.name = name;
		//System.out.println("Peer Name: " + name + "\nAddress: " + InetAddress.getLocalHost().getHostAddress()  +"\nListening at port " + portNumber + "\n-----------------------");
	}

	public String getName(){
		return name;
	}
	
	public int getListeningPort(){
		return portNumber;
	}
	
	public String getAddress(){
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			return "Unable to get Address";
		}
	}
	public void connectToRouter(String routerAddress, int routerPort) throws IOException{
		this.routerAddress = routerAddress;
		this.routerPort = routerPort;
		this.routerSocket = new Socket(routerAddress, routerPort);
		messageIn = new BufferedReader(new InputStreamReader(routerSocket.getInputStream()));
		messageOut = new PrintWriter(routerSocket.getOutputStream(), true); 
		messageOut.println("Type:PeerHandshake|Source:Peer|Name:" + this.getName() + "|Address:" + this.getAddress() + "|Port:" +this.getListeningPort());
		
		packet = messageIn.readLine();
		parsedPacket = PacketParser.parse(packet);
		System.out.println("Router: " + parsedPacket.get("Message"));
	}
	public void connectToPeer(String name) throws IOException{
		String peerName = name;
		messageOut.println("Type:PeerRequest|Source:Peer|PeerName:" + peerName);
		packet = messageIn.readLine();
		parsedPacket = PacketParser.parse(packet);
		
		if(parsedPacket.get("Message").equals("Success")){
			String peerAddress = parsedPacket.get("Address");
			int peerPort = Integer.parseInt(parsedPacket.get("Port"));
			peerSocket = new Socket(peerAddress, peerPort);
			dataIn = new DataInputStream(peerSocket.getInputStream());
			dataOut = new DataOutputStream(peerSocket.getOutputStream());
		}
		else{
			System.out.println("Unable to connect to peer " + peerName);
		}
			
	}
	public void p2pSendInt(int message) throws IOException{
		dataOut.writeInt(message);

	}
	public String p2pReadString() throws IOException{
		return dataIn.readUTF();
	}
	public int p2pReadInt() throws IOException{
		return dataIn.readInt();
	}
	
	public void sendFile(String fileName) throws IOException {
		byte[] bytes = {};
		dataOut.writeUTF(fileName);	
		Path filePath = Paths.get("files/",fileName);
		try{
			bytes = Files.readAllBytes(filePath);
			
		}
		catch(IOException e){
			dataOut.writeInt(-1);
			System.err.println("file: " + fileName + " not found.");
			return;
		}
		
        dataOut.writeInt(bytes.length); //sends length of file to server
        dataIn.readUTF(); //blocks until server is ready for file transfer
  	   	dataOut.write(bytes,0,bytes.length); // transfers file
	}

	public void receiveFile(String fileFullName) throws IOException {
		dataOut.writeUTF(fileFullName);
		String fileName = "";
		String extension = "";
		try{
			fileName = fileFullName.substring(0, fileFullName.lastIndexOf("."));
			extension = fileFullName.substring(fileFullName.lastIndexOf(".")+1, fileFullName.length());
		}
		catch(Exception e){
			System.err.println("file " + fileFullName +" not valid name. Please include file extension");
		}
		
		int fileSize = 0;
		fileSize = dataIn.readInt();
		if(fileSize == -1){
			System.err.println("file: " + fileFullName + " not found.\n");
			return;
		}
		File file = new File("files/" + fileFullName);
		String newFileName;
		int fileNumber = 1;
		while(!file.createNewFile()){
			newFileName = fileName + "(" + fileNumber + ")";
			file = new File("files/" + newFileName + "." + extension);
			fileNumber++;
		}
		
		FileOutputStream fos = new FileOutputStream(file);
		
		dataOut.writeUTF("Ready for file transfer");
		byte[] buffer = new byte[fileSize];
		
		//metrics
		List<Integer> packageSizes = new ArrayList<Integer>();
		int totalBytes = 0;
		int numPackages = 0;
		long totalTimeMillis = 0;
		int packageSize = 0;
		long startTime = System.currentTimeMillis();
		
		while((packageSize = dataIn.read(buffer)) != -1){
			//metrics
			totalBytes += packageSize;
			packageSizes.add(packageSize);
			numPackages++;
			
			//writes file
			fos.write(buffer,0,packageSize);
			fos.flush();
			
			if(totalBytes >= fileSize){
				break;
			}
		}
		totalTimeMillis = System.currentTimeMillis() - startTime;
		
		//record metrics to log
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("metrics.csv", true)))) {
			int sum = 0;
			for(int i = 0; i < packageSizes.size(); i++){
				sum += packageSizes.get(i);
			}
			float avgPackageSize = sum/packageSizes.size();
		    out.println(fileFullName+","+totalBytes+","+totalTimeMillis+"," + numPackages + "," + Collections.max(packageSizes)+ "," + Collections.min(packageSizes) + "," + avgPackageSize);
		}catch (IOException e) {
		  
		}
		fos.close();
		System.out.println("File Received. Saved in files/ directory");
	}

	public void closePeerConnection() {
		try {
			peerSocket.close();
			dataIn.close();
			dataOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void closeRouterConnection(){
		try{
			messageOut.println("Type:Disconnect|Source:Peer|Name:" + this.getName());
			//Confirm Router has disconnected
			messageIn.readLine();
			routerSocket.close();
			messageIn.close();
			messageOut.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public boolean isConnectedToPeer(){
		if(peerSocket == null)
			return false;
		return !peerSocket.isClosed();
	}

	public String getRouterAddress() {
		return routerAddress;
	}

	public void setRouterAddress(String routerAddress) {
		this.routerAddress = routerAddress;
	}

	public int getRouterPort() {
		return routerPort;
	}

	public void setRouterPort(int routerPort) {
		this.routerPort = routerPort;
	}
	
}
