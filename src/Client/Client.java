package Client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import Interfaces.BulletinBoard;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class Client extends Application {

	private static SecretKey symmetricKeyAB;
	private static SecretKey symmetricKeyBA;
	private static int indexAB = -1;
	private static byte[] tagAB;
	private static int indexBA = -1;
	private static byte[] tagBA;
	private static String nameBA;
	private static SecureRandom secureRandomGenerator;
	private static BulletinBoard bb;
	private static String separator = "#§_§#";
	//private List<Byte> seperatorByteList;
	private static String name;
	private static Scanner scan;

	private Thread rt;

	//GUI
	@FXML private TextField sendMessages;
	@FXML private TextArea receivedMessages = new TextArea();
	private  ObservableList<String> messageList;
	private Scene scene;

	public Client(){}

	@Override
	public void start(Stage primaryStage) throws Exception {
		scan = new Scanner(System.in);
		System.out.println("Geef gebruikersnaam in:");
		name = scan.nextLine();
		/*byte[] separatorByte = "#§_§#".getBytes();
		seperatorByteList = new ArrayList<>();
		for(int i = 0 ; i < separatorByte.length ; i++){
			seperatorByteList.add(separatorByte[i]);
		}*/
		startClient();

		Parent root = FXMLLoader.load(getClass().getResource("Client.fxml"));
		primaryStage.setTitle("Chatbox van "+name);
		scene = new Scene(root, 500, 500);
		primaryStage.setScene(scene);
		primaryStage.show();

		// CHECKEN ALS ER AL CONTACTEN ZIJN
		rt = new ReceiveThread(this);
		rt.start();

		// EXIT PROGRAM
		primaryStage.setOnCloseRequest((WindowEvent event1) -> {
			rt.interrupt();
		});
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
		String msg=sendMessages.getText();
		sendMessages.clear();
		sendAB(msg);
		receivedMessages.appendText(msg + "\n");
	}

	@FXML protected void handleSelectContactButtonAction(ActionEvent event) {

	}

	@FXML protected  void handleNewContactButtonAction(ActionEvent event){
		// Create the custom dialog.
		Dialog<List<String>> dialog = new Dialog<>();
		dialog.setTitle("Login Dialog");
		dialog.setHeaderText("Voeg een persoon toe");

// Set the icon (must be included in the project).
		//dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString()));

// Set the button types.
		ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		PasswordField password = new PasswordField();
		password.setPromptText("Paswoord");
		TextField username = new TextField();
		username.setPromptText("Gebruikersnaam");
		PasswordField userpassword = new PasswordField();
		userpassword.setPromptText("Paswoord");

		grid.add(new Label("Eigen paswoord:"), 0, 0);
		grid.add(password, 1, 0);
		grid.add(new Label("Gebruikersnaam:"), 0, 1);
		grid.add(username, 1, 1);
		grid.add(new Label("Gebruiker paswoord:"), 0, 2);
		grid.add(userpassword, 1, 2);

// Enable/Disable login button depending on whether a username was entered.
		Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
		loginButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
		AtomicBoolean usernameEmpty = new AtomicBoolean(true);
		AtomicBoolean passwordEmpty = new AtomicBoolean(true);
		AtomicBoolean userpasswordEmpty = new AtomicBoolean(true);
		username.textProperty().addListener((observable, oldValue, newValue) -> {
			usernameEmpty.set(newValue.trim().isEmpty());
			if(!usernameEmpty.get() && !userpasswordEmpty.get() && !passwordEmpty.get()){
				loginButton.setDisable(false);
			}
			else{
				loginButton.setDisable(true);
			}
		});
		password.textProperty().addListener((observable, oldValue, newValue) -> {
			passwordEmpty.set(newValue.trim().isEmpty());
			if(!usernameEmpty.get() && !userpasswordEmpty.get() && !passwordEmpty.get()){
				loginButton.setDisable(false);
			}
			else{
				loginButton.setDisable(true);
			}
		});
		userpassword.textProperty().addListener((observable, oldValue, newValue) -> {
			userpasswordEmpty.set(newValue.trim().isEmpty());
			if(!usernameEmpty.get() && !userpasswordEmpty.get() && !passwordEmpty.get()){
				loginButton.setDisable(false);
			}
			else{
				loginButton.setDisable(true);
			}
		});

		dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
		Platform.runLater(() -> username.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == loginButtonType) {
				List<String> result = new ArrayList<String>();
				result.add(password.getText());
				result.add(username.getText());
				result.add(userpassword.getText());
				return result;
			}
			return null;
		});

		Optional<List<String>> result = dialog.showAndWait();

		result.ifPresent(usernamePassword -> {
			System.out.println("Eigen Paswoord=" + usernamePassword.get(0) + ", Gebruikersnaam=" + usernamePassword.get(1) + ", Gebruiker paswoord=" + usernamePassword.get(2));
			addContact(usernamePassword.get(0), usernamePassword.get(1), usernamePassword.get(2));
		});
	}

	private void startClient(){
		try {
			// fire to localhost port 1099
			Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);

			// search for CounterService
			bb = (BulletinBoard) myRegistry.lookup("ChatService");
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
			if(indexBA > -1 && tagBA != null && symmetricKeyBA != null){
				value = bb.get(indexBA, tagBA);
			}
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
		//System.out.println(msg);
		//messageList.add(msg);
		try {
			TextArea ta = (TextArea) scene.lookup("#receivedMessages");
			ta.appendText(msg + "\n");
		}
		catch (NullPointerException npe){
			System.out.println("FOUT!!!");
		}
	}

	public void addContact(String password, String username, String userPassword){
		try {
			secureRandomGenerator = new SecureRandom();
			// Hash van userinput nemen? ==> generatedPassword
			byte[] generatedPassword = hashFunction(password.getBytes());
			// new SecureRandom(userinput.getBytes()) ==> nextBytes()? ==> generatedPassword
			//byte[] generatedPassword = secureRandomGenerator.generateSeed(32);

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

			generatedPassword = hashFunction(userPassword.getBytes());
			passwordString = Base64.getEncoder().encodeToString(generatedPassword);
			indexBA = Math.abs(passwordString.hashCode()%25);
			tagBA = generatedPassword;
			secureRandomGenerator = new SecureRandom(generatedPassword);
			salt = new byte[32];
			secureRandomGenerator.nextBytes(salt);
			keySpec = new PBEKeySpec(passwordString.toCharArray(), salt, 65536, 256);
			temp = factory.generateSecret(keySpec);
			symmetricKeyBA = new SecretKeySpec(temp.getEncoded(), 0, temp.getEncoded().length,  "AES");
			nameBA = username;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}

	public String getNameBA() {
		return nameBA;
	}

	public static void main(String[] args) {
		launch(args);
	}
}