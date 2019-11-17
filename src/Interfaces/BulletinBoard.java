package Interfaces;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BulletinBoard extends Remote{
	public void add(int index, byte[] value, byte[] tag) throws RemoteException;
	public byte[] get(int index, byte[] tag) throws RemoteException;
	public int getMailboxSize() throws RemoteException;
}
