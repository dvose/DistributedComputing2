package CS4253.peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
		
		boolean connected = true;
		while(connected){
			option = in.readInt();
			switch(option){
			case 1:
				String name = in.readUTF();
				System.out.println("name " + name);
				try{
					receiveFile(name);
				}
				catch(IOException e){
					System.err.println("Unable to receive file");
				}
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
					System.err.println("Unable to send file");
				}
				break;
			case 3:
				closePeerConnection();
				connected = false;
				break;
			default:
				out.writeUTF("invalid option");
				break;
			}
		}
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
		byte[] bytes = {};
		try{ 
			bytes = Files.readAllBytes(filePath);
		}
		catch(IOException e){
			out.writeInt(-1);
			return;
		}
        out.writeInt(bytes.length); //sends length of file to server
        in.readUTF(); //blocks until server is ready for file transfer
  	   	out.write(bytes,0,bytes.length); // transfers file
	}
	
	public void receiveFile(String fileFullName) throws IOException {
		String fileName = "";
		String extension = "";
		try{
			fileName = fileFullName.substring(0, fileFullName.lastIndexOf("."));
			extension = fileFullName.substring(fileFullName.lastIndexOf(".")+1, fileFullName.length());
		}
		catch(StringIndexOutOfBoundsException e){
			System.err.println("file " + fileFullName + "not a valid file. Please include extension");
		}
		
		int fileSize = 0;
		fileSize = in.readInt();
		
		//if no file found return
		if(fileSize == -1){
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
		
		out.writeUTF("Ready for file transfer");
		int totalBytes = 0;
		byte[] buffer = new byte[fileSize];
	
		int packageSize = 0;
		while((packageSize = in.read(buffer)) != -1){
			totalBytes += packageSize;
			//writes file
			fos.write(buffer,0,packageSize);
			fos.flush();
			
			if(totalBytes >= fileSize){
				break;
			}
		}
		fos.close();
	}
	public void closePeerConnection() throws IOException{
		clientSocket.close();
	}
}
