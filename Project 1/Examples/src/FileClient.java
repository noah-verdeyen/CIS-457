import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class FileClient {
    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();

        // localhost for testing
        Socket sock = new Socket("127.0.0.1", 13267);
        System.out.println("Connecting...");
        InputStream is = sock.getInputStream();
        // receive file
        new FileClient().receiveFile(is);
        OutputStream os = sock.getOutputStream();
        //new FileClient().send(os);
        long end = System.currentTimeMillis();
   

        sock.close();
    }


    public void send(OutputStream os) throws Exception {
        // sendfile
        File myFile = new File("/home/kennemat/Documents/CIS 457/test");
        byte[] mybytearray = new byte[(int) myFile.length() + 1];
        FileInputStream fis = new FileInputStream(myFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(mybytearray, 0, mybytearray.length);
        System.out.println("Sending...");
        os.write(mybytearray, 0, mybytearray.length);
        os.flush();
    }

    public void receiveFile(InputStream is) throws Exception {
        int filesize = 3;
        int bytesRead;
        int current = 0;
        byte[] mybytearray = new byte[filesize];

        FileOutputStream fos = new FileOutputStream("receivedFile");
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bytesRead = is.read(mybytearray, 0, mybytearray.length);
        current = bytesRead;
//        System.out.println(Arrays.toString(mybytearray));
        //Loop through reading byte into mybytearray, start at current (which will start at the max).
        //Every iteration, current is incremented and less is rea
        /*
        do { 
            bytesRead = is.read(mybytearray, current,
                    (mybytearray.length - current));
            
           System.out.println(Arrays.toString(mybytearray) + "\nint is " + bytesRead);
            if (bytesRead >= 0)
                current += bytesRead;
        } while (bytesRead > -1);
*/
        bos.write(mybytearray, 0, current);
        bos.flush();
        bos.close();
    }
} 