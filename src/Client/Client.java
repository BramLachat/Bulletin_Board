package Client;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;

import Interfaces.BulletinBoard;

import javax.crypto.SecretKey;


public class Client {

	//private SecretKey symmetricKeyAB;
	private PrivateKey privateKeyA;
	private PublicKey publicKeyA;
	private PublicKey publicKeyB;
	private int indexAB;
	private byte[] tagAB;
	private int indexBA;
	private byte[] tagBA;
	private static SecureRandom secureRandomGenerator;
	private BulletinBoard bb;
	
	public Client(){
		try {
			secureRandomGenerator = SecureRandom.getInstance("SHA1PRNG", "SUN");
			// 24 is willekeurig gekozen in overeenkomst met lengte van de List in BulletinBoardImplementation
			indexAB = secureRandomGenerator.nextInt(24);
			tagAB = secureRandomGenerator.generateSeed(256);
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
			keyPairGen.initialize(2048);
			KeyPair pair = keyPairGen.generateKeyPair();
			privateKeyA = pair.getPrivate();
			publicKeyA = pair.getPublic();
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

			SendThread st = new SendThread(this);
			st.start();
			
			//ReadThread rt = new ReadThread(impl, userName);
			//rt.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void keyDerivationFunction(){

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
		// TODO: Implementatie van sendAB function (zie figure 2 paper)
	}

	public static void main(String[] args) {
		Client client = new Client();
		client.startClient();
	}
}