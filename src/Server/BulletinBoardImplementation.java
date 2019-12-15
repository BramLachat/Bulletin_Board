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
	private int[] numberOfMessages;

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


	public BulletinBoardImplementation() throws RemoteException {
		// De 25 plaatsen zijn random gekozen
		mailbox = new ArrayList<>(mailboxSize);
		numberOfMessages = new int[mailboxSize];

		for(int i = 0 ; i < mailboxSize ; i++){
			mailbox.add(new HashMap<>());
		}
	}

	@Override
	public synchronized void add(int index, byte[] value, byte[] tag) throws RemoteException {
		numberOfMessages[index]++;
		HashMap<String, byte[]> cell = mailbox.get(index);
		cell.put(Base64.getEncoder().encodeToString(tag), value);
		System.out.println("added index: " + index + " // tag: " + Base64.getEncoder().encodeToString(tag));
		for(int i = 0 ; i < numberOfMessages.length ; i++){
			if(numberOfMessages[i] > 0){
				System.out.print(i + ": " + numberOfMessages[i] + "\t");
			}
		}
		System.out.println();
	}

	@Override
	public synchronized byte[] get(int index, byte[] tag) throws RemoteException {
		HashMap<String, byte[]> cell = mailbox.get(index);
		byte[] value = null;
		try {
			byte[] hastTag = generateHash(tag);
			value = cell.remove(Base64.getEncoder().encodeToString(hastTag));
			if(value != null){
				numberOfMessages[index]--;
				System.out.println("get index: " + index + " // tag: " + Base64.getEncoder().encodeToString(hastTag));
				for(int i = 0 ; i < numberOfMessages.length ; i++){
					if(numberOfMessages[i] > 0){
						System.out.print(i + ": " + numberOfMessages[i] + "\t");
					}
				}
				System.out.println();
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

	public List<HashMap<String, byte[]>> getMailbox() {
		return mailbox;
	}
}
