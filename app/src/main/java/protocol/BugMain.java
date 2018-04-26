package protocol;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

enum test implements Serializable {
    A(10), B(1);
    private int index[]=new int[10];

    test(int index) {
        //this.index = index;
        for (int i = 0; i < 10; i++) {
            this.index[i]=index;
        }
    }

    public int getIndex() {
        return index[0];
    }

    public void setIndex(int index) {
        //this.index = index;
        for (int i = 0; i < 10; i++) {
            this.index[i]=index;
        }
    }

    @Override
    public String toString() {
        return "test{" + "index=" + index[0] + '}';
    }
}

public class BugMain {
    public static void main(String[] args) {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(8083);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Socket client = null;
        while (true) {
            try {
                //收到一个client，首先将其push进去vec列表，

                client = serverSocket.accept();


                test C = test.A;
                C.setIndex(1000);


                System.out.println(C);

                OutputStream outputStream = client.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(C);

                objectOutputStream.flush();
                //clientTmp.send2Client("Hello,tcp client\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
