package discomputing.peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ListeningThread extends Thread {
	private ServerSocket listeningSocket = null;
	private Socket clientSocket = null;
	private DataInputStream in= null;
	private DataOutputStream out = null;
	
	public ListeningThread(int portNumber){
		try{
			listeningSocket = new ServerSocket(portNumber);
		}
		catch(IOException e){
			System.err.println("Unable to listen at port: " + portNumber);
			System.exit(-1);
		}
	}
	public void run(){
		while(true){
				try {
					clientSocket = listeningSocket.accept();
					System.out.println("Connect at " + clientSocket.getInetAddress());
					in = new DataInputStream(clientSocket.getInputStream());
					out = new DataOutputStream(clientSocket.getOutputStream());
					peerHandler();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
		}
	}
	
	/*
	 * method: peerHandler
	 * description: handles connected peer's requests
	 */
	public void peerHandler() throws IOException{
		int option = 0;
		out.writeUTF("Connected to Peer");
		
		while((option = in.readInt()) != 3){
			switch(option){
			case 1:
				System.out.println("Receiving file from peer");
				break;
			case 2:
				ArrayList<String> fileNames = listFiles();
				out.writeInt(fileNames.size());
				for(String fileName : fileNames){
					out.writeUTF(fileName);
				}
				try{
					sendFile(in.readUTF());
				}
				catch(IOException e){
					
				}
			default:
				out.writeUTF("invalid option");
				break;
			}
		}
		clientSocket.close();
	}
	public ArrayList<String> listFiles(){
		ArrayList<String> results = new ArrayList<String>();
		File[] files = new File("files").listFiles();
		for (File file : files) {
		    if (file.isFile()) {
		        results.add(file.getName());
		    }
		}
		return results;
	}
	
	public void sendFile(String fileName) throws IOException{
		Path filePath = Paths.get("files/",fileName);
		byte[] bytes = Files.readAllBytes(filePath);
		byte[] complete = new byte[3];//flag that file is finished
        complete = "done".getBytes();
        out.writeUTF(fileName.substring(fileName.lastIndexOf(".")+1, fileName.length())); //Sends the file extension the peer
        out.flush();
        System.out.println("Server: " + in.readUTF());
        out.writeInt(bytes.length); //sends length of file to server
        out.flush();
        System.out.println(bytes.length);
        System.out.println("Server: " + in.readUTF()); //blocks until server is ready for file transfer
  	   	out.write(bytes,0,bytes.length); // transfers file
  	   	out.flush();
  	   	out.write(complete,0,3); //sends flag
	}
}
