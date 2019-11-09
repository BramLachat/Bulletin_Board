package Server;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import Interfaces.BulletinBoard;

public class BulletinBoardImplementation extends UnicastRemoteObject implements BulletinBoard{

	private List<HashSet<String>> mailbox;

	public BulletinBoardImplementation() throws RemoteException {
		// De 25 plaatsen zijn random gekozen
		mailbox = new ArrayList<>(25);
	}

	@Override
	public void send(String msg) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String receive() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
