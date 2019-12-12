package Client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Scanner;

import Interfaces.BulletinBoard;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class Client extends Application {

	private static SecretKey symmetricKeyAB;
	private static SecretKey symmetricKeyBA;
	private static int indexAB;
	private static byte[] tagAB;
	private static int indexBA;
	private static byte[] tagBA;
	private static SecureRandom secureRandomGenerator;
	private static BulletinBoard bb;
	private static String separator;
	//private List<Byte> seperatorByteList;
	private static String name;
	private static Scanner scan;

	private SendThread st;
	private Thread rt;

	//GUI
	@FXML private TextField sendMessages;
	@FXML private TextArea receivedMessages = new TextArea();
	private  ObservableList<String> messageList;
	private Scene scene;

	/*public Client(){
		messageList = FXCollections.observableArrayList();
		messageList.addListener((ListChangeListener<String>) lsl -> {
			for(String s : lsl.getList()) {
				receivedMessages.appendText(s);
			}
		});
	}*/

	public Client(){}

	@Override
	public void start(Stage primaryStage) throws Exception {
		scan = new Scanner(System.in);
		System.out.println("Geef gebruikersnaam in:");
		name = scan.nextLine();
		separator = "#§_§#";
		/*byte[] separatorByte = "#§_§#".getBytes();
		seperatorByteList = new ArrayList<>();
		for(int i = 0 ; i < separatorByte.length ; i++){
			seperatorByteList.add(separatorByte[i]);
		}*/
		try {
			secureRandomGenerator = new SecureRandom();
			byte[] generatedPassword = secureRandomGenerator.generateSeed(32);
			String passwordString = Base64.getEncoder().encodeToString(generatedPassword);
			System.out.println("PASSWORD: " + passwordString);
			secureRandomGenerator = new SecureRandom(generatedPassword);
			byte[] salt = new byte[32];
			secureRandomGenerator.nextBytes(salt);
			indexAB = Math.abs(passwordString.hashCode()%25);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec keySpec = new PBEKeySpec(passwordString.toCharArray(), salt, 65536, 256);
			SecretKey temp = factory.generateSecret(keySpec);
			symmetricKeyAB = new SecretKeySpec(temp.getEncoded(), 0, temp.getEncoded().length,  "AES");
			tagAB = generatedPassword;

			// 24 is willekeurig gekozen in overeenkomst met lengte van de List in BulletinBoardImplementation
			//indexAB = secureRandomGenerator.nextInt(24);
			//tagAB = secureRandomGenerator.generateSeed(256);
			//KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			//keyGenerator.init(256);
			//symmetricKeyAB = keyGenerator.generateKey();
			//System.out.println("indexAB: " + indexAB + "\n\n" + "tagAB: " + Base64.getEncoder().encodeToString(tagAB) + "\n\n" + "symmetricKeyAB: " + Base64.getEncoder().encodeToString(symmetricKeyAB.getEncoded()));
			System.out.println("Bump: Geef passwoord in: ");
			passwordString = scan.nextLine();
			generatedPassword = Base64.getDecoder().decode(passwordString);
			indexBA = Math.abs(passwordString.hashCode()%25);
			tagBA = generatedPassword;
			secureRandomGenerator = new SecureRandom(generatedPassword);
			salt = new byte[32];
			secureRandomGenerator.nextBytes(salt);
			keySpec = new PBEKeySpec(passwordString.toCharArray(), salt, 65536, 256);
			temp = factory.generateSecret(keySpec);
			symmetricKeyBA = new SecretKeySpec(temp.getEncoded(), 0, temp.getEncoded().length,  "AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		startClient();

		Parent root = FXMLLoader.load(getClass().getResource("Client.fxml"));
		primaryStage.setTitle("Chatbox van "+name);
		scene = new Scene(root, 300, 275);
		primaryStage.setScene(scene);
		primaryStage.show();

		rt = new Thread(new ReceiveThread(this));
		rt.start();
		/*textArea = new TextArea();
		TextField textField = new TextField();
		TextField indexField = new TextField();

		VBox vBox = new VBox(textArea,textField);
		vBox.setSpacing(15);

		BlockingQueue<Integer> stdInQueue = new LinkedBlockingQueue<>();
		System.setIn(new InputStream() {

			@Override
			public int read() throws IOException {
				try {
					int c = stdInQueue.take().intValue();
					return c;
				} catch (InterruptedException exc) {
					Thread.currentThread().interrupt();
					return -1 ;
				}
			}
		});

		textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.ENTER)) {

					for (char c : textField.getText().toCharArray()) {
						stdInQueue.add(new Integer(c));
					}
					stdInQueue.add(new Integer('\n'));
					textField.clear();
					indexField.setText(""+indexAB);

				}
			}
		});

		Scene scene = new Scene(vBox,800,800);
		primaryStage.setScene(scene);*/
	}

	@FXML protected void handleSubmitButtonAction(ActionEvent event) {
		System.out.println("Chatbox opgestart!");
		//String msg = scan.nextLine();

		String msg=sendMessages.getText();
		sendMessages.clear();
		/*try {
			int i ;
			while ((i = System.in.read()) != -1) {
				//i=System.in.read();
				msg = msg+((char)i);
			}
		} catch (IOException exc) {
			exc.printStackTrace();
		}*/

		if(msg.compareToIgnoreCase("exit") == 0) {
			sendAB(name + " heeft de chat verlaten!");
			System.out.println("U heeft de chat verlaten!");
		}
		else {
			sendAB(msg);
			receivedMessages.appendText(msg + "\n");
		}
	}

	private void startClient(){
		try {
			// fire to localhost port 1099
			Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);

			// search for CounterService
			bb = (BulletinBoard) myRegistry.lookup("ChatService");
			/*System.out.println("Sender/Receiver?");
			String sendOrReceive = scan.nextLine();
			switch (sendOrReceive) {
				case "Sender": SendThread st = new SendThread(this);
					st.start();
					break;
				case "Receiver": ReceiveThread rt = new ReceiveThread(this);
					rt.start();
					break;
			}*/

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// https://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/CryptoSpec.html#PBEEx (Using Password-Based Encryption)
	// https://www.baeldung.com/java-password-hashing (alternatief met salt, aantal iteraties en key lengte --> 5.2. Implementing PBKDF2 in Java: PBKDF = Password Based Key Derivation Function)
	// If you're deriving a key from a master key, as opposed to deriving a key from a password, then you should use a key derivation function such as HKDF,
	// not a password-based key derivation function such as PBKDF2. That's not insecure per se, but it's massively inefficient.
	// (https://stackoverflow.com/questions/4513433/deriving-a-secret-from-a-master-key-using-jce-jca)
	protected SecretKey keyDerivationFunction(SecretKey key){
		KeySpec spec = new PBEKeySpec(Base64.getEncoder().encodeToString(key.getEncoded()).toCharArray());
		SecretKey sk = null;
		SecretKeyFactory factory = null;
		try {
			factory = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_256"); // Password Based Encryption (PBE)
			sk = factory.generateSecret(spec);
			byte[] hashKey = hashFunction(sk.getEncoded()); // Nodig omdat nieuwe key (sk) anders langer is dan de oude
			sk = new SecretKeySpec(hashKey, 0, hashKey.length, "AES");
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return sk;
	}

	protected byte[] hashFunction(byte[] tag) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return digest.digest(tag);
	}

	protected void sendAB(String m){
		//System.out.println("bericht verzonden");
		// Implementatie van sendAB function (zie figure 2 paper)
		int nextIndexAB = secureRandomGenerator.nextInt(24);
		byte[] nextTagAB = secureRandomGenerator.generateSeed(256);
		byte[] value = createMessage(m, nextIndexAB, nextTagAB);
		try {
			Cipher cipher = Cipher.getInstance("AES");
			//System.out.println("Gebruikte sleutel: " + Base64.getEncoder().encodeToString(symmetricKeyAB.getEncoded()));
			cipher.init(Cipher.ENCRYPT_MODE, symmetricKeyAB);
			byte[] valueEncrypted = cipher.doFinal(value);
			//byte[] hastTagAB = hashFunction(new String(tagAB).getBytes());
			byte[] hastTagAB = hashFunction(tagAB);
			bb.add(indexAB, valueEncrypted, hastTagAB);
		} catch (Exception e) {
			e.printStackTrace();
		}
		indexAB = nextIndexAB;
		tagAB = nextTagAB;
		symmetricKeyAB = keyDerivationFunction(symmetricKeyAB);
	}

	private byte[] createMessage(String m, int nextIndexAB, byte[] nextTagAB) {
		String message = m + separator + nextIndexAB + separator + Base64.getEncoder().encodeToString(nextTagAB);
		return message.getBytes();
		/*List<Byte> byteList = new ArrayList<>();
		byte[] mBytes = m.getBytes();
		for(int i = 0 ; i < mBytes.length ; i++) {
			byteList.add(mBytes[i]);
		}
		byteList.addAll(seperatorByteList);
		byte[] nextIndexABBytes = Integer.toString(nextIndexAB).getBytes();
		for(int i = 0 ; i < nextIndexABBytes.length ; i++) {
			byteList.add(nextIndexABBytes[i]);
		}
		byteList.addAll(seperatorByteList);
		System.out.println("nextTagAB.length: " + nextTagAB.length);
		for(int i = 0 ; i < nextTagAB.length ; i++) {
			byteList.add(nextTagAB[i]);
		}
		byte[] message = new byte[byteList.size()];
		for(int i = 0 ; i < byteList.size() ; i++) {
			message[i] = byteList.get(i);
		}
		return message;*/
	}

	protected String receiveBA() throws InterruptedException {
		// Implementatie van receiveAB function (zie figure 2 paper)
		Thread.sleep(1000);
		String res = null;
		byte[] value = null;
		try {
			value = bb.get(indexBA, tagBA);
			if(value != null){
				Cipher cipher = Cipher.getInstance("AES");
				//cipher.init(Cipher.DECRYPT_MODE, symmetricKeyBA);
				cipher.init(Cipher.DECRYPT_MODE, symmetricKeyBA);
				byte[] decryptedValue = cipher.doFinal(value);
				String[] message = new String(decryptedValue).split("#§_§#");
				indexBA = Integer.parseInt(message[1]);
				tagBA = Base64.getDecoder().decode(message[2]);
				symmetricKeyBA = keyDerivationFunction(symmetricKeyBA);
				res = message[0];
				/*if(message[2].compareTo(new String(tagAB)) == 0){
					System.out.println("TRUE");
					byte[] tag = message[2].getBytes();
					System.out.println(Base64.getEncoder().encodeToString(new String(tagAB).getBytes()));
					System.out.println(Base64.getEncoder().encodeToString(tag));
				}
				else{
					System.out.println("FALSE");
				}*/
			}
		} catch (RemoteException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return res;
	}

	public void printToTextArea(String msg){
		System.out.println(msg);
		//messageList.add(msg);
		try {
			TextArea ta = (TextArea) scene.lookup("#receivedMessages");
			ta.appendText(msg + "\n");
		}
		catch (NullPointerException npe){
			System.out.println("FOUT!!!");
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}