import Interfaces.BulletinBoard;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Scanner;

public class TestMain {

    private static SecretKey symmetricKeyAB;
    private static SecretKey symmetricKeyBA;
    private static int indexAB;
    private static byte[] tagAB;
    private static int indexBA;
    private static byte[] tagBA;
    private static SecureRandom secureRandomGenerator;
    private static String separator;
    private static String name;
    private static Scanner scan;

    public static void main(String [] args){

        Scanner scanner = new Scanner(System.in);

        System.out.println("Geef een wachtwoord in");
        String wachtwoord1 = scanner.nextLine();
        char[] ww1char = wachtwoord1.toCharArray();
        System.out.println("Geef een tweede wachtwoord in");
        String wachtwoord2 = scanner.nextLine();
        char[] ww2char = wachtwoord2.toCharArray();

        byte[] salt1 = new byte[8];
        byte[] salt2 = new byte[8];

        // Salt ook met SecureRandom genereren aan de hand van wachtwoord?
        salt1[0] = 40;
        salt1[1] = 64;
        salt1[2] = 86;
        salt1[3] = 127;
        salt1[4] = 3;
        salt1[5] = 8;
        salt1[6] = 14;
        salt1[7] = 113;


        salt2[0] = 40;
        salt2[1] = 64;
        salt2[2] = 86;
        salt2[3] = 127;
        salt2[4] = 3;
        salt2[5] = 8;
        salt2[6] = 14;
        salt2[7] = 113;

        //salt moet aan beide kanten gelijk zijn (en mag publiek zijn) waarom??

        try{

            //ZELFDE INDEX OP WACHTWOORD
            // SecureRandom secureRandomGenerator = new SecureRandom(wachtwoord1.getBytes());
            indexAB = wachtwoord1.hashCode()%25;
            System.out.println("indexAB: " + indexAB);
            indexBA = wachtwoord2.hashCode()%25;

            if(indexAB == indexBA){
                System.out.println("indexen zijn gelijk: "+indexAB);
            } else{
                System.out.println("indexen zijn niet gelijk");
            }

            //ZELFDE KEY OP WACHTWOORD
            SecretKeyFactory factory1 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec keySpec1 = new PBEKeySpec(ww1char, salt1, 65536, 256);
            SecretKey temp1 = factory1.generateSecret(keySpec1);
            SecretKey secretKey1 = new SecretKeySpec(temp1.getEncoded(), "AES");

            SecretKeyFactory factory2 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec keySpec2 = new PBEKeySpec(ww2char, salt2, 65536, 256);
            SecretKey temp2 = factory2.generateSecret(keySpec2);
            SecretKey secretKey2 = new SecretKeySpec(temp2.getEncoded(), "AES");

            if(secretKey1.equals(secretKey2)){
                System.out.println("KEYS ZIJN GELIJK: "+secretKey1);
            } else{
                System.out.println("KEYS ZIJN NIET GELIJK");
            }

            //ZELFDE TAGS PER WACHTWOORD

            // BRAM
            SecureRandom secureRandomGenerator = new SecureRandom(wachtwoord1.getBytes());
            tagAB = secureRandomGenerator.generateSeed(256);

            // WOUTER
            int deling = 256/wachtwoord1.length();
            String stringVoorByteArrayGegenereerdUitPaswoord1 = wachtwoord1;
            String stringVoorByteArrayGegenereerdUitPaswoord2 = wachtwoord2;

            for (int i = 0; i < deling+1 ; i++) {
                stringVoorByteArrayGegenereerdUitPaswoord1 = stringVoorByteArrayGegenereerdUitPaswoord1 + wachtwoord1;
                stringVoorByteArrayGegenereerdUitPaswoord2 = stringVoorByteArrayGegenereerdUitPaswoord2 + wachtwoord2;
            }

            tagAB = new byte[256];
            tagBA = new byte[256];

            for (int i = 0; i < tagAB.length; i++) {
                tagAB[i] = stringVoorByteArrayGegenereerdUitPaswoord1.getBytes()[i];
                tagBA[i] = stringVoorByteArrayGegenereerdUitPaswoord2.getBytes()[i];
            }

            boolean tagsZijnGelijk = true;
            for (int i = 0; i < 256; i++) {
                if(tagAB[i] != tagBA[i]){
                    tagsZijnGelijk=false;
                }
            }

            if(tagsZijnGelijk){
                System.out.println("TAGS ZIJN GELIJK: "+tagAB.toString());
            } else{
                System.out.println("TAGS ZIJN NIET GELIJK");

                for (int i = 0; i < tagAB.length; i++) {
                    System.out.println(tagAB[i] + "  " + tagBA[i]);
                }
            }


            } catch (Exception e){
            e.printStackTrace();
        }

        scanner.close();
    }
}
