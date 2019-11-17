package Client;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import Interfaces.BulletinBoard;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class Client {

	private SecretKey symmetricKeyAB;
	private SecretKey symmetricKeyBA;
	private int indexAB;
	private byte[] tagAB;
	private int indexBA;
	private byte[] tagBA;
	private static SecureRandom secureRandomGenerator;
	private BulletinBoard bb;
	private String separator;
	//private List<Byte> seperatorByteList;
	private String name;
	private Scanner scan;
	
	public Client(){
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
			secureRandomGenerator = SecureRandom.getInstance("SHA1PRNG", "SUN");
			// 24 is willekeurig gekozen in overeenkomst met lengte van de List in BulletinBoardImplementation
			indexAB = secureRandomGenerator.nextInt(24);
			tagAB = secureRandomGenerator.generateSeed(256);
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(256);
			symmetricKeyAB = keyGenerator.generateKey();
			System.out.println("indexAB: " + indexAB + "\n\n" + "tagAB: " + Base64.getEncoder().encodeToString(tagAB) + "\n\n" + "symmetricKeyAB: " + Base64.getEncoder().encodeToString(symmetricKeyAB.getEncoded()));
			System.out.println("Bump: Geef indexBA in: ");
			indexBA = Integer.parseInt(scan.nextLine());
			System.out.println("Bump: Geef tagBA in: ");
			tagBA = Base64.getDecoder().decode(scan.nextLine());
			//tagBA = new String(tagBA).getBytes();
			System.out.println("Bump: Geef symmetricKeyBA in: ");
			String symmetricKey = scan.nextLine();
			symmetricKeyBA = new SecretKeySpec(Base64.getDecoder().decode(symmetricKey), 0, Base64.getDecoder().decode(symmetricKey).length, "AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
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
			SendThread st = new SendThread(this);
			st.start();
			ReceiveThread rt = new ReceiveThread(this);
			rt.start();
			
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
		Thread.sleep(3000);
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

	public String getName() {
		return name;
	}

	public static void main(String[] args) {
		Client client = new Client();
		client.startClient();
	}
}