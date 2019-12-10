package Server;
import Client.Client;
import Interfaces.BulletinBoard;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javafx.scene.control.ListView;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server{ //extends Application {

	static BulletinBoardImplementation bulletinBoardImplementation;

	public static void main(String[] args) {
		Server main = new Server();
		try {
			bulletinBoardImplementation = new BulletinBoardImplementation();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		main.startServer();

		//Client client1 = new Client(args);
		//Client client2 = new Client(args);

		//javaFX applicatie oproepen vanuit de bulletinboard klasse
		//bulletinBoardImplementation.start(args);
	}
	
	private void startServer() {
		try {
			// create on port 1099
			Registry registry = LocateRegistry.createRegistry(1099);
			// create a new service named CounterService
			registry.rebind("ChatService", bulletinBoardImplementation);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Server is ready");
	}

	/*@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Chatbox");
		StackPane root = new StackPane();
		primaryStage.setScene(new Scene(root,800,600));

		observableList = bulletinBoardImplementation.getWaardenMailboxes();

		observableList.addListener(new ListChangeListener<Integer>() {
			@Override
			public void onChanged(Change<? extends Integer> c) {
				System.out.println("verandering");
				System.out.println(bulletinBoardImplementation.getWaardenMailboxes().get(5));
			}
		});

		listView = new ListView();
		listView.setItems(observableList);

		root.getChildren().add(listView);
		primaryStage.show();
	}*/
}
