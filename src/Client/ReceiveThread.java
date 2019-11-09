package Client;

public class ReceiveThread extends Thread {

    private Client c;

    protected ReceiveThread(Client c) {
        this.c = c;
    }

    public void run(){
        String msg = c.receiveAB();
    }
}
