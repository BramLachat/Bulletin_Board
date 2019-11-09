package Client;

import Interfaces.BulletinBoard;

import java.io.Console;
import java.rmi.RemoteException;
import java.util.Scanner;

public class WriteThread extends Thread {
    private BulletinBoard chat;
    private String userName;
    private Scanner scan;

    public WriteThread(BulletinBoard chat, String userName) {
        this.chat = chat;
        this.userName = userName;
        scan = new Scanner(System.in);
    }

    public void run() {

    }
}
