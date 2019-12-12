package Client;

public class ReceiveThread implements Runnable {

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
                    //System.out.println("Antwoord: " + msg);
                    c.printToTextArea("Antwoord: " + msg);

                    if(msg.endsWith("heeft de chat verlaten!")){
                        exit = true;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
