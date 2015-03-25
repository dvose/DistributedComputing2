package discomputing.peer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;


/* Class: Peer
 * Description: A Peer is a node that will create connections with other Peers through SRouter.
 * 				
 */
public class Peer {
	private ServerSocket listeningSocket = null;
	private Socket routerSocket = null;
	private Socket peerSocket = null;
	private BufferedReader messageIn = null;
	private BufferedWriter messageOut = null;
	private DataInputStream dataIn = null;
	private DataOutputStream dataOut = null;
	private String name = "";
	
	public Peer(String name, int portNumber) throws IOException{
		ListeningThread lThread = new ListeningThread(portNumber);
		lThread.start();
		this.name = name;
		System.out.println("Peer Name: " + name + "\nAddress: " + InetAddress.getLocalHost().getHostAddress()  +"\nListening at port " + portNumber + "\n-----------------------");
	}

	public String getName(){
		return name;
	}
	
	public int getListeningPort(){
		return listeningSocket.getLocalPort();
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
	
	public void sendFile() {
		// TODO Auto-generated method stub
		
	}

	public void receiveFile(String fileName) throws IOException {
		dataOut.writeUTF(fileName);
		String extension = dataIn.readUTF();
		dataOut.writeUTF("extension received");
		int fileSize = 0;
		Date date = new Date();
		FileOutputStream fos = new FileOutputStream("files/TEST_FILE" + date.getTime() + "." + extension);
		fileSize = dataIn.readInt();
		System.out.println("File Size: " + fileSize);
		dataOut.writeUTF("Ready for file transfer");
		int totalBytes = 0;
		byte[] buffer = new byte[fileSize];
	
		int packageSize = 0;
		while((packageSize = dataIn.read(buffer)) != -1){
			totalBytes += packageSize;
			//writes file
			fos.write(buffer,0,packageSize);
			fos.flush();
			
			System.out.println(totalBytes + " = " + fileSize);
			
			if(totalBytes >= fileSize){
				totalBytes -= 3;
				break;
			}
		}

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
