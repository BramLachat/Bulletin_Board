package Client;

public class ReceiveThread extends Thread {

    private Client c;

    protected ReceiveThread(Client c) {
        this.c = c;
    }

    public void run(){
        while(true) {
            try {
                String msg = c.receiveBA();
                if(msg != null) System.out.print("ReceiveThread: msg: " + msg + " // ");
                else{
                    System.out.print("null // ");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
