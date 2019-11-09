package Server;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
	public static void main(String[] args) {
		Server main = new Server();
		main.startServer();
	}
	
	private void startServer() {
		try {
			// create on port 1099
			Registry registry = LocateRegistry.createRegistry(1099);
			// create a new service named CounterService
			registry.rebind("ChatService", new BulletinBoardImplementation());
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Server is ready");
	}
}
