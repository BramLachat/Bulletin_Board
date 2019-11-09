package Client;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

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
			
			ReceiveThread rt = new ReceiveThread(this);
			rt.start();
			
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
		//zie generatePart1Message ==> van alle delen bytearray maken ==> samen in ArrayList steken ==> alles uit arraylist terug in bytearray steken samen met de tekens nodig om de verschillende delen te kunnen onderscheiden!!!
		List<Byte> byteList = new ArrayList<>();
		byte[] msg = m.getBytes();
		for(int i = 0 ; i < msg.length ; i++) {
			byteList.add(msg[i]);
		}
		byte[] indexABBytes = Integer.toString(indexAB).getBytes();
		for(int i = 0 ; i < indexABBytes.length ; i++) {
			byteList.add(indexABBytes[i]);
		}
		for(int i = 0 ; i < tagAB.length ; i++) {
			byteList.add(tagAB[i]);
		}
		byte[] message = new byte[byteList.size()];

		//byte[] msg = m.getBytes() + "$@_@$" + indexAB + "$@_@$" + tagAB;
		//System.out.println(msg);
	}

	public String receiveAB() {
		// TODO: Implementatie van receiveAB function (zie figure 2 paper)
		return null;
	}

	public static void main(String[] args) {
		Client client = new Client();
		client.startClient();
	}
}