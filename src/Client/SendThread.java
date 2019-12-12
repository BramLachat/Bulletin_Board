package Client;

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
            //String msg = scan.nextLine();

            String msg="";
            /*try {
                int i ;
                while ((i = System.in.read()) != -1) {
                    //i=System.in.read();
                    msg = msg+((char)i);
                }
            } catch (IOException exc) {
                exc.printStackTrace();
            }*/

            if(msg.compareToIgnoreCase("exit") == 0) {
                exit = true;
                //c.sendAB(c.getName() + " heeft de chat verlaten!");
                System.out.println("U heeft de chat verlaten!");
            }
            else {
                c.sendAB(msg);
            }
        }
    }
}
