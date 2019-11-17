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
        boolean exit = false;
        System.out.println("Chatbox opgestart!");
        while(!exit){
            String msg = scan.nextLine();
            if(msg.compareToIgnoreCase("exit") == 0) {
                exit = true;
                c.sendAB(c.getName() + " heeft de chat verlaten!");
                System.out.println("U heeft de chat verlaten!");
            }
            else {
                c.sendAB(msg);
            }
        }
    }
}
