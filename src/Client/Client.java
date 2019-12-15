package Client;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import Interfaces.BulletinBoard;
import com.fasterxml.jackson.core.*;
import javafx.application.Application;
import javafx.application.Platform;
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

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class Client extends Application {

	private static SecretKey dataEncryptionKey;

	private static SecretKey symmetricKeyAB;
	private static List<SecretKey> symmetricKeysAB = new ArrayList<>();
	private static SecretKey symmetricKeyBA;
	private static List<SecretKey> symmetricKeysBA = new ArrayList<>();
	private static int indexAB = -1;
	private static List<Integer> indicesAB = new ArrayList<>();
	private static byte[] tagAB;
	private static List<byte[]> tagsAB = new ArrayList<>();
	private static int indexBA = -1;
	private static List<Integer> indicesBA = new ArrayList<>();
	private static byte[] tagBA;
	private static List<byte[]> tagsBA = new ArrayList<>();
	private static String nameBA;
	private static HashMap<String, Integer> namesBA = new HashMap<>();
	private static List<TextArea> messages = new ArrayList<>();

	private static SecureRandom secureRandomGenerator = new SecureRandom();
	private static BulletinBoard bb;
	private static String separator = "#§_§#";
	private static String name;

	private Thread rt;

	//GUI
	@FXML private TextField sendMessages;
	@FXML private TextArea receivedMessages = new TextArea();
	@FXML private Label contactPerson;
	private Scene scene;

	public Client(){
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try{
			setEncryptionKey();
			readFromFile();
		}
		catch (IOException ioe){
		}

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
			try {
				// SAVE DATA FROM CURRENT CONTACTPERSON
				try{
					int previousIndex = namesBA.get(nameBA);
					symmetricKeysAB.set(previousIndex,symmetricKeyAB);
					indicesAB.set(previousIndex, indexAB);
					tagsAB.set(previousIndex, tagAB);
					symmetricKeysBA.set(previousIndex, symmetricKeyBA);
					indicesBA.set(previousIndex, indexBA);
					tagsBA.set(previousIndex, tagBA);
				}
				catch (NullPointerException npe){}
				writeToFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			rt.interrupt();
		});
	}

	@FXML protected void handleSubmitButtonAction(ActionEvent event) {
		String msg=sendMessages.getText();
		sendMessages.clear();
		try{
			sendAB(msg);
		}
		catch (NullPointerException e){
			e.printStackTrace();
		}
		receivedMessages.appendText(msg + "\n");
	}

	@FXML protected void handleSelectContactButtonAction(ActionEvent event) {
		ChoiceDialog<String> dialog = new ChoiceDialog<>(null, namesBA.keySet());
		dialog.setTitle("Kies contactpersoon");
		dialog.setHeaderText("Selecteer een contactpersoon");
		dialog.setContentText("Keuze:");
		dialog.setGraphic(new ImageView(this.getClass().getResource("user.png").toString()));

// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			try{

// SAVE DATA FROM CURRENT CONTACTPERSON
				int previousIndex = namesBA.get(nameBA);
				symmetricKeysAB.set(previousIndex,symmetricKeyAB);
				indicesAB.set(previousIndex, indexAB);
				tagsAB.set(previousIndex, tagAB);
				symmetricKeysBA.set(previousIndex, symmetricKeyBA);
				indicesBA.set(previousIndex, indexBA);
				tagsBA.set(previousIndex, tagBA);

// SAVE CURRENT CONVERSATION
				TextArea currentMessages = new TextArea(receivedMessages.getText());
				messages.set(previousIndex, currentMessages);
				receivedMessages.clear();
			}
			catch (NullPointerException npe){
			}

			int newIndex = namesBA.get(result.get());
			symmetricKeyAB = symmetricKeysAB.get(newIndex);
			indexAB = indicesAB.get(newIndex);
			tagAB = tagsAB.get(newIndex);
			symmetricKeyBA = symmetricKeysBA.get(newIndex);
			indexBA = indicesBA.get(newIndex);
			tagBA = tagsBA.get(newIndex);
			nameBA = result.get();

			try {
// RELOAD PREVIOUS CONVERSATION
				TextArea previousMessages = new TextArea(messages.get(namesBA.get(nameBA)).getText());
				receivedMessages.setText(previousMessages.getText());
			}
			catch (IndexOutOfBoundsException ioobe){
				messages.add(new TextArea());
			}
			contactPerson.setText("Contactpersoon: " + nameBA);
		}
	}

	@FXML protected  void handleNewContactButtonAction(ActionEvent event){
		// Create the custom dialog.
		Dialog<List<String>> dialog = new Dialog<>();
		dialog.setTitle("Login Dialog");
		dialog.setHeaderText("Voeg een persoon toe");

// Set the icon (must be included in the project).
		dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString()));

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
		AtomicReference<String> s = new AtomicReference<>();
		username.textProperty().addListener((observable, oldValue, newValue) -> {
			usernameEmpty.set(newValue.trim().isEmpty());
			s.set(newValue);
			s.get();
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
		Platform.runLater(() -> password.requestFocus());

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
			//System.out.println("Eigen Paswoord=" + usernamePassword.get(0) + ", Gebruikersnaam=" + usernamePassword.get(1) + ", Gebruiker paswoord=" + usernamePassword.get(2));

			// SAVE DATA FROM CURRENT CONTACTPERSON
			try{
				int previousIndex = namesBA.get(nameBA);
				symmetricKeysAB.set(previousIndex,symmetricKeyAB);
				indicesAB.set(previousIndex, indexAB);
				tagsAB.set(previousIndex, tagAB);
				symmetricKeysBA.set(previousIndex, symmetricKeyBA);
				indicesBA.set(previousIndex, indexBA);
				tagsBA.set(previousIndex, tagBA);

// SAVE CURRENT CONVERSATION
				TextArea currentMessages = new TextArea(receivedMessages.getText());
				messages.set(namesBA.get(nameBA), currentMessages);
			}
			catch (NullPointerException npe){}

			addContact(usernamePassword.get(0), usernamePassword.get(1), usernamePassword.get(2));

			receivedMessages.clear();
			contactPerson.setText("Contactpersoon: " + nameBA);
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
	protected SecretKey keyDerivationFunction(String password){
		SecureRandom sr = new SecureRandom(password.getBytes());
		byte[] salt = new byte[32];
		sr.nextBytes(salt);
		KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
		SecretKey sk = null;
		SecretKeyFactory factory = null;
		try {
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			SecretKey temp = factory.generateSecret(keySpec);
			sk = new SecretKeySpec(temp.getEncoded(), 0, temp.getEncoded().length,  "AES");
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		System.out.println("keyDerivationFunction: returned sk: " + Base64.getEncoder().encodeToString(sk.getEncoded()));
		return sk;

		/*KeySpec spec = new PBEKeySpec(Base64.getEncoder().encodeToString(key.getEncoded()).toCharArray());
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
		return sk;*/
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
		// Implementatie van sendAB function (zie figure 2 paper)
		byte[] nextTagAB = secureRandomGenerator.generateSeed(32);
		int nextIndexAB = nextTagAB.hashCode()%25;
		byte[] value = createMessage(m, nextIndexAB, nextTagAB);
		try {
			byte[] valueEncrypted = encrypt(value, symmetricKeyAB);
			//byte[] hastTagAB = hashFunction(new String(tagAB).getBytes());
			byte[] hastTagAB = hashFunction(tagAB);
			bb.add(indexAB, valueEncrypted, hastTagAB);
		} catch (Exception e) {
			e.printStackTrace();
		}
		indexAB = nextIndexAB;
		tagAB = nextTagAB;
		symmetricKeyAB = keyDerivationFunction(Base64.getEncoder().encodeToString(symmetricKeyAB.getEncoded()));
	}

	private byte[] createMessage(String m, int nextIndexAB, byte[] nextTagAB) {
		String message = m + separator + nextIndexAB + separator + Base64.getEncoder().encodeToString(nextTagAB);
		return message.getBytes();
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
				byte[] decryptedValue = decrypt(value, symmetricKeyBA);
				String[] message = new String(decryptedValue).split(separator);
				indexBA = Integer.parseInt(message[1]);
				tagBA = Base64.getDecoder().decode(message[2]);
				symmetricKeyBA = keyDerivationFunction(Base64.getEncoder().encodeToString(symmetricKeyBA.getEncoded()));
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

	protected void printToTextArea(String msg){
		try {
			TextArea ta = (TextArea) scene.lookup("#receivedMessages");
			ta.appendText(msg + "\n");
		}
		catch (NullPointerException npe){
		}
	}

	protected void addContact(String password, String username, String userPassword){
		// Hash van userinput nemen? ==> generatedPassword
		//byte[] generatedPassword = hashFunction(password.getBytes());
		// new SecureRandom(userinput.getBytes()) ==> nextBytes()? ==> generatedPassword
		//byte[] generatedPassword = secureRandomGenerator.generateSeed(32);

		//String passwordString = Base64.getEncoder().encodeToString(generatedPassword);
		//System.out.println("PASSWORD: " + password);
		indexAB = Math.abs(password.hashCode()%25);
		indicesAB.add(indexAB);
		symmetricKeyAB = keyDerivationFunction(password);
		symmetricKeysAB.add(symmetricKeyAB);
		tagAB = hashFunction(password.getBytes());
		tagsAB.add(tagAB);

		// 24 is willekeurig gekozen in overeenkomst met lengte van de List in BulletinBoardImplementation
		//indexAB = secureRandomGenerator.nextInt(24);
		//tagAB = secureRandomGenerator.generateSeed(256);
		//KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		//keyGenerator.init(256);
		//symmetricKeyAB = keyGenerator.generateKey();
		//System.out.println("indexAB: " + indexAB + "\n\n" + "tagAB: " + Base64.getEncoder().encodeToString(tagAB) + "\n\n" + "symmetricKeyAB: " + Base64.getEncoder().encodeToString(symmetricKeyAB.getEncoded()));

		indexBA = Math.abs(userPassword.hashCode()%25);
		indicesBA.add(indexBA);
		tagBA = hashFunction(userPassword.getBytes());
		tagsBA.add(tagBA);
		symmetricKeyBA = keyDerivationFunction(userPassword);
		symmetricKeysBA.add(symmetricKeyBA);
		nameBA = username;
		namesBA.put(nameBA, namesBA.size());
		messages.add(new TextArea());
	}

	protected String getNameBA() {
		return nameBA;
	}

	protected void writeToFile() throws IOException {
		JsonFactory factory = new JsonFactory();

		JsonGenerator generator = factory.createGenerator(
				new File(name + ".json"), JsonEncoding.UTF8);

		List<String> names = new ArrayList<>(namesBA.keySet());
		for(int i = 0 ; i < namesBA.size() ; i++) {
			generator.writeStartObject();
			try {
				generator.writeBinaryField("name", encrypt(name.getBytes(), dataEncryptionKey));
				generator.writeBinaryField("symmetricKeyAB", encrypt(symmetricKeysAB.get(i).getEncoded(), dataEncryptionKey));
				generator.writeBinaryField("indexAB", encrypt(indicesAB.get(i).toString().getBytes(), dataEncryptionKey));
				generator.writeBinaryField("tagAB", encrypt(tagsAB.get(i), dataEncryptionKey));
				generator.writeBinaryField("nameBA", encrypt(names.get(i).getBytes(), dataEncryptionKey));
				generator.writeBinaryField("symmetricKeyBA", encrypt(symmetricKeysBA.get(i).getEncoded(), dataEncryptionKey));
				generator.writeBinaryField("indexBA", encrypt(indicesBA.get(i).toString().getBytes(), dataEncryptionKey));
				generator.writeBinaryField("tagBA", encrypt(tagsBA.get(i), dataEncryptionKey));
				generator.writeEndObject();
			} catch (NoSuchPaddingException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			}
		}
		generator.close();
	}

	protected void readFromFile() throws IOException {
		File file = new File(name + ".json");

		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(file);

		while(!parser.isClosed()){
			JsonToken jsonToken = parser.nextToken();

			if(JsonToken.FIELD_NAME.equals(jsonToken)){
				String fieldName = parser.getCurrentName();

				jsonToken = parser.nextToken();

				try {
					if ("name".equals(fieldName)) {
						//name = parser.getValueAsString();
						//System.out.println("name: " + name);
					} else if ("symmetricKeyAB".equals(fieldName)) {
						symmetricKeysAB.add(new SecretKeySpec(decrypt(parser.getBinaryValue(), dataEncryptionKey), 0, decrypt(parser.getBinaryValue(), dataEncryptionKey).length, "AES"));
						//System.out.println("symmetricKeyAB: " + parser.getBinaryValue());
					} else if ("indexAB".equals(fieldName)) {
						indicesAB.add(Integer.parseInt(new String(decrypt(parser.getBinaryValue(), dataEncryptionKey))));
						//System.out.println("indexAB: " + parser.getValueAsInt());
					} else if ("tagAB".equals(fieldName)) {
						tagsAB.add(decrypt(parser.getBinaryValue(), dataEncryptionKey));
						//System.out.println("tagAB: " + parser.getBinaryValue());
					} else if ("nameBA".equals(fieldName)) {
						namesBA.put(new String(decrypt(parser.getBinaryValue(), dataEncryptionKey)), namesBA.size());
					} else if ("symmetricKeyBA".equals(fieldName)) {
						symmetricKeysBA.add(new SecretKeySpec(decrypt(parser.getBinaryValue(), dataEncryptionKey), 0, decrypt(parser.getBinaryValue(), dataEncryptionKey).length, "AES"));
					} else if ("indexBA".equals(fieldName)) {
						indicesBA.add(Integer.parseInt(new String(decrypt(parser.getBinaryValue(), dataEncryptionKey))));
					} else if ("tagBA".equals(fieldName)) {
						tagsBA.add(decrypt(parser.getBinaryValue(), dataEncryptionKey));
					}
				}
				catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
					e.printStackTrace();
					System.out.println("ONGELDIG WACHTWOORD!");
					System.exit(0);
				}
			}
		}
	}

	protected void setEncryptionKey(){
		// Create the custom dialog.
		Dialog<List<String>> dialog = new Dialog<>();
		dialog.setTitle("Startup Dialog");
		dialog.setHeaderText("Geef gebruikersnaam en sleutel in:");

// Set the icon (must be included in the project).
		dialog.setGraphic(new ImageView(this.getClass().getResource("key.png").toString()));

// Set the button types.
		ButtonType loginButtonType = new ButtonType("Inloggen", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		PasswordField password = new PasswordField();
		password.setPromptText("Sleutel");
		PasswordField password2 = new PasswordField();
		password2.setPromptText("Bevestig sleutel");
		TextField username = new TextField();
		username.setPromptText("Gebruikersnaam");

		grid.add(new Label("Gebruikersnaam: "), 0, 0);
		grid.add(username, 1, 0);
		grid.add(new Label("Encryptiesleutel:"), 0, 1);
		grid.add(password, 1, 1);
		grid.add(new Label("Encryptiesleutel:"), 0, 2);
		grid.add(password2, 1, 2);

// Enable/Disable login button depending on whether a username was entered.
		Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
		loginButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
		AtomicBoolean passwordEmpty = new AtomicBoolean(true);
		AtomicBoolean password2Empty = new AtomicBoolean(true);
		AtomicBoolean usernameEmpty = new AtomicBoolean(true);
		AtomicReference<String> pwd = new AtomicReference<>();
		AtomicReference<String> pwd2 = new AtomicReference<>();
		username.textProperty().addListener((observable, oldValue, newValue) -> {
			usernameEmpty.set(newValue.trim().isEmpty());
			if(!passwordEmpty.get() && !password2Empty.get() && pwd.get().equals(pwd2.get()) && !usernameEmpty.get()){
				loginButton.setDisable(false);
			}
			else{
				loginButton.setDisable(true);
			}
		});
		password.textProperty().addListener((observable, oldValue, newValue) -> {
			passwordEmpty.set(newValue.trim().isEmpty());
			pwd.set(newValue);
			if(!passwordEmpty.get() && !password2Empty.get() && pwd.get().equals(pwd2.get()) && !usernameEmpty.get()){
				loginButton.setDisable(false);
			}
			else{
				loginButton.setDisable(true);
			}
		});
		password2.textProperty().addListener((observable, oldValue, newValue) -> {
			password2Empty.set(newValue.trim().isEmpty());
			pwd2.set(newValue);
			if(!passwordEmpty.get() && !password2Empty.get() && pwd.get().equals(pwd2.get()) && !usernameEmpty.get()){
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
				return result;
			}
			return null;
		});

		Optional<List<String>> result = dialog.showAndWait();

		result.ifPresent(usernamePassword -> {
			//System.out.println("Encryptiesleutel=" + usernamePassword);

			dataEncryptionKey = keyDerivationFunction(usernamePassword.get(0));
			name = usernamePassword.get(1);
		});
	}

	protected byte[] encrypt(byte[] value, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance("AES");
		//System.out.println("Gebruikte sleutel: " + Base64.getEncoder().encodeToString(symmetricKeyAB.getEncoded()));
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] valueEncrypted = cipher.doFinal(value);
		return valueEncrypted;
	}

	protected byte[] decrypt(byte[] value, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance("AES");
		//cipher.init(Cipher.DECRYPT_MODE, symmetricKeyBA);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] decryptedValue = cipher.doFinal(value);
		return decryptedValue;
	}

	public static void main(String[] args) {
		launch(args);
	}
}