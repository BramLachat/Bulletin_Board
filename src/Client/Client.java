package Client;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import Interfaces.BulletinBoard;


public class Client {
	
	public static void main(String[] args) {
		Client main = new Client();
		Scanner scan = new Scanner(System.in);
		System.out.println("Geef gebruikersnaam in:");
		String userName = scan.nextLine();
		main.startClient(userName);
	}
	
	private void startClient(String userName){
		try {
			// fire to localhost port 1099
			Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);

			// search for CounterService
			BulletinBoard bb = (BulletinBoard) myRegistry.lookup("ChatService");

			// call server's method
			//System.out.println(impl.berekenSom(10, 5));
			//System.out.println(impl.berekenVerschil(10,5));
			
			//WriteThread wt = new WriteThread(impl, userName);
			//wt.start();
			
			//ReadThread rt = new ReadThread(impl, userName);
			//rt.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}