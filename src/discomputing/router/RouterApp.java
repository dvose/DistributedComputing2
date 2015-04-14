package discomputing.router;

import java.net.InetAddress;
import java.util.InputMismatchException;
import java.util.Scanner;

import discomputing.router.branch.BranchRouter;
import discomputing.router.root.RootRouter;

public class RouterApp {
	static Scanner in = new Scanner(System.in);
	static Router router = null;

	public static void main(String[] args) {
		boolean running = true;
		String type = null;
		int port = 0;
		
		System.out.print("Router SETUP\n-----------------------\nListening Port: ");
		try{
			port = in.nextInt();
		}
		catch(InputMismatchException e){
			System.err.println("Port must be an integer");
			System.exit(-1);
		}
		in.nextLine();
		System.out.print("Router Type (root or branch): ");
		while(true){	
			type = in.nextLine();
			
			if(type.equals("root")){
				router = new RootRouter(port);
				break;
			}
			else if(type.equals("branch")){
				String rootAddress = null;
				int rootPort = 0;
				
				System.out.print("Address of Root Router: ");
				rootAddress = in.nextLine();
				
				System.out.print("Port number of Root Router: ");
				rootPort = in.nextInt();
				in.nextLine();
				
				try {
					router = new BranchRouter(port, rootAddress, rootPort);
				} catch (Exception e) {
					System.err.println("Unable to create router");
					System.exit(-1);
				}
				break;
			}
			else{
				System.out.println("Please select root or branch");
			}
		}
		
		while(running){
			try {
				System.out.println("Router is running at " + InetAddress.getLocalHost().getHostAddress() + " Port: " + port + "\n Enter ^C to exit");
				router.listen();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String option = in.nextLine();
			if(option == "exit"){
				running = false;
			}
		}
	}	
}
