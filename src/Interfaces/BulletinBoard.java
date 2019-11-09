package Interfaces;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BulletinBoard extends Remote{
	public void send(String msg)throws RemoteException;
	public String receive()throws RemoteException;
}
