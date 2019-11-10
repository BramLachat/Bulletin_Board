package Client;

import Interfaces.BulletinBoard;

import java.rmi.RemoteException;
import java.util.Scanner;

public class SendThread extends Thread {
    private Scanner scan;
    private Client c;

    protected SendThread(Client c) {
        scan = new Scanner(System.in);
        this.c = c;
    }

    public void run() {
        System.out.println("Geef een bericht in: ");
        String msg = scan.nextLine();
        c.sendAB(msg);
    }
}
