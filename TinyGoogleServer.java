import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class TinyGoogleServer {
    private List<IndexingHelper> indexingHelperList;
    private List<QueryHelper> queryHelperList;
    private List<String> indexedPaths;
    public Semaphore indexLock;
    public Semaphore queryLock;

    public TinyGoogleServer() {
        this.indexingHelperList = new ArrayList<>();
        this.queryHelperList = new ArrayList<>();
        this.indexedPaths = new ArrayList<>();
        this.indexLock = new Semaphore(1, true);
        this.queryLock = new Semaphore(1, true);
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
