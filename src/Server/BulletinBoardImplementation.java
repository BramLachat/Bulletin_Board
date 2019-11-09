package Server;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import Interfaces.BulletinBoard;

public class BulletinBoardImplementation extends UnicastRemoteObject implements BulletinBoard{

	public BulletinBoardImplementation() throws RemoteException {
		// TODO Auto-generated constructor stub
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
