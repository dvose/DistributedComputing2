package discomputing.peer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/* Class: Peer
 * Description: A Peer is a node that will create connections with other Peers through SRouter.
 * 				
 */
public class Peer {
	private Socket routerSocket = null;
	private Socket peerSocket = null;
	private BufferedReader messageIn = null;
	private BufferedWriter messageOut = null;
	private DataInputStream dataIn = null;
	private DataOutputStream dataOut = null;
	private String name = "";
	private int portNumber;
	
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
		routerSocket = new Socket(routerAddress, routerPort);
	}
	
	public void connectToPeer(String peerAddress, int peerPort) throws IOException{
		peerSocket = new Socket(peerAddress, peerPort);
		dataIn = new DataInputStream(peerSocket.getInputStream());
		dataOut = new DataOutputStream(peerSocket.getOutputStream());	
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
		dataOut.writeUTF(fileName);
		Path filePath = Paths.get("files/",fileName);
		byte[] bytes = Files.readAllBytes(filePath);
        dataOut.writeInt(bytes.length); //sends length of file to server
        dataIn.readUTF(); //blocks until server is ready for file transfer
  	   	dataOut.write(bytes,0,bytes.length); // transfers file
	}

	public void receiveFile(String fileFullName) throws IOException {
		dataOut.writeUTF(fileFullName);
		String fileName;
		try{
			fileName = fileFullName.substring(0, fileFullName.lastIndexOf("."));
		}
		catch(Exception e){
			System.err.println("file" + fileFullName +" not valid name. Please include file extension");
			return;
		}
		String extension = fileFullName.substring(fileFullName.lastIndexOf(".")+1, fileFullName.length());
		int fileSize = 0;
		File file = new File("files/" + fileFullName);
		
		String newFileName;
		int fileNumber = 1;
		while(!file.createNewFile()){
			newFileName = fileName + "(" + fileNumber + ")";
			file = new File("files/" + newFileName + "." + extension);
			fileNumber++;
		}
		
		FileOutputStream fos = new FileOutputStream(file);
		fileSize = dataIn.readInt();
		dataOut.writeUTF("Ready for file transfer");
		int totalBytes = 0;
		byte[] buffer = new byte[fileSize];
	
		int packageSize = 0;
		while((packageSize = dataIn.read(buffer)) != -1){
			totalBytes += packageSize;
			//writes file
			fos.write(buffer,0,packageSize);
			fos.flush();
			
			if(totalBytes >= fileSize){
				break;
			}
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
	
	public boolean isConnectedToPeer(){
		return !peerSocket.isClosed();
	}
	
}
