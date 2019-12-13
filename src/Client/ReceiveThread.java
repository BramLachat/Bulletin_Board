package Client;

import java.util.concurrent.atomic.AtomicBoolean;

public class ReceiveThread extends Thread {

    private Client c;

    protected ReceiveThread(Client c) {
        this.c = c;
    }

    public void run(){
        boolean running = true;
        while(running) {
            try {
                String msg = c.receiveBA();
                if(msg != null) {
                    //System.out.println("Antwoord: " + msg);
                    c.printToTextArea(c.getNameBA() + ": " + msg);
                }
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }
}
