import java.net.ServerSocket;
import java.net.Socket;

public class TinyGoogleServer {


    public TinyGoogleServer() {

    }

    public void run(int port) {
        // **** setup server socket ****
        final ServerSocket serverSock;
        try
        {
            serverSock = new ServerSocket(port);
        }
        catch(Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }

        // **** listening to incoming messages ****
        while (true) {
            try {
                Socket socket = serverSock.accept();      //listen
                ServerThread serverThread = new ServerThread(socket, this);
                serverThread.start();
            }
            catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace(System.err);
                return;
            }
        }
    }

    public static void main(String[] args) {
        TinyGoogleServer server = new TinyGoogleServer();
        server.run(1234);
    }
}
