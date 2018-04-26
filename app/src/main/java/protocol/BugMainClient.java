package protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;


public class BugMainClient {
    public static void main(String[] args) {
        Socket server = null;
        try {
            server = new Socket("127.0.0.1",8083);
        } catch (IOException e) {
            e.printStackTrace();
        }


        InputStream inputStream = null;
        try {
            inputStream = server.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            test recvMsg = (test) objectInputStream.readObject();

            if(recvMsg!=null)
                    System.out.println(recvMsg);

             else System.out.println("recv null");


        } catch (IOException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
