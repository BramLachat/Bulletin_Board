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

public class BulletinBoardImplementation extends UnicastRemoteObject implements BulletinBoard{

	private int mailboxSize = 25;
	private List<HashMap<String, byte[]>> mailbox;

	public BulletinBoardImplementation() throws RemoteException {
		// De 25 plaatsen zijn random gekozen
		mailbox = new ArrayList<>(mailboxSize);
		for(int i = 0 ; i < mailboxSize ; i++){
			mailbox.add(new HashMap<>());
		}
	}

	@Override
	public void add(int index, byte[] value, byte[] tag) throws RemoteException {
		HashMap<String, byte[]> cell = mailbox.get(index);
		cell.put(Base64.getEncoder().encodeToString(tag), value);
	}

	@Override
	public byte[] get(int index, byte[] tag) throws RemoteException {
		HashMap<String, byte[]> cell = mailbox.get(index);
		byte[] value = null;
		try {
			byte[] hastTag = generateHash(tag);
			value = cell.remove(Base64.getEncoder().encodeToString(hastTag));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return value;
	}

	private byte[] generateHash(byte[] tag) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return digest.digest(tag);
	}
}
