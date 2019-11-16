package Client;

public class ReceiveThread extends Thread {

    private Client c;

    protected ReceiveThread(Client c) {
        this.c = c;
    }

    public void run(){
        boolean exit = false;
        while(!exit) {
            try {
                String msg = c.receiveBA();
                if(msg != null) {
                    System.out.print("ReceiveThread: msg: " + msg + " // ");
                    if(msg.endsWith("heeft de chat verlaten!")){
                        exit = true;
                    }
                }
                else{
                    System.out.print("null // ");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
