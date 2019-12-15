package Server;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import Interfaces.BulletinBoard;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class BulletinBoardImplementation extends UnicastRemoteObject implements BulletinBoard{

	private int mailboxSize = 25;
	private List<HashMap<String, byte[]>> mailbox;
	private static ObservableList<Integer> waardenMailboxes;

	/*public void start(String [] args){
		Application.launch(InnerClassVoorJavaFX.class,args);
	}

	public static class InnerClassVoorJavaFX extends Application{

		@Override
		public void start(Stage primaryStage) throws Exception {
			primaryStage.setTitle("Chatbox");
			StackPane root = new StackPane();
			primaryStage.setScene(new Scene(root,800,600));

			ListView listView = new ListView();

			waardenMailboxes.addListener(new ListChangeListener<Integer>() {
				@Override
				public void onChanged(Change<? extends Integer> c) {
					System.out.println("verandering");
					listView.setItems(waardenMailboxes);
				}
			});

			listView.setItems(waardenMailboxes);

			root.getChildren().add(listView);
			primaryStage.show();
		}
	}*/


	public List<HashMap<String, byte[]>> getMailbox() {
		return mailbox;
	}

	public BulletinBoardImplementation() throws RemoteException {
		// De 25 plaatsen zijn random gekozen
		mailbox = new ArrayList<>(mailboxSize);
		ArrayList<Integer> nullijst = new ArrayList<>();

		for(int i = 0 ; i < mailboxSize ; i++){
			mailbox.add(new HashMap<>());
			nullijst.add(0); //zal dus ook lengte 25 hebben
		}

		waardenMailboxes = FXCollections.observableArrayList(nullijst);
	}


	public ObservableList<Integer> getWaardenMailboxes(){
		waardenMailboxes.set(5,waardenMailboxes.get(5)+1);
		return waardenMailboxes;
	}

	@Override
	public synchronized void add(int index, byte[] value, byte[] tag) throws RemoteException {
		waardenMailboxes.set(index,waardenMailboxes.get(index)+1);
		HashMap<String, byte[]> cell = mailbox.get(index);
		cell.put(Base64.getEncoder().encodeToString(tag), value);
		System.out.println("added index: " + index + " // tag: " + Base64.getEncoder().encodeToString(tag));
	}

	@Override
	public synchronized byte[] get(int index, byte[] tag) throws RemoteException {
		HashMap<String, byte[]> cell = mailbox.get(index);
		byte[] value = null;
		try {
			byte[] hastTag = generateHash(tag);
			value = cell.remove(Base64.getEncoder().encodeToString(hastTag));
			if(value != null){
				System.out.println("get index: " + index + " // tag: " + Base64.getEncoder().encodeToString(hastTag));
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return value;
	}

	private byte[] generateHash(byte[] tag) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return digest.digest(tag);
	}

	public int getMailboxSize() {
		return mailboxSize;
	}
}
