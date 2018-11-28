import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Semaphore;

public class TinyGoogleServer {
    public List<Socket> indexingHelperSocketList;
    public List<ObjectInputStream> indexingHelperInputList;
    public List<ObjectOutputStream> indexingHelperOutputList;
    public List<Socket> queryHelperSocketList;
    public List<ObjectInputStream> queryHelperInputList;
    public List<ObjectOutputStream> queryHelperOutputList;

    final public String file1 = "ab.txt";
    final public String file2 = "cdefg.txt";
    final public String file3 = "hijkl.txt";
    final public String file4 = "mnopq.txt";
    final public String file5 = "t.txt";
    final public String file6 = "rsuvwxyz.txt";


    private List<String> indexedPaths;
    public Semaphore indexLock;
    public Semaphore queryLock;

    public TinyGoogleServer() {
        this.indexingHelperSocketList = new ArrayList<>();
        this.indexingHelperInputList = new ArrayList<>();
        this.indexingHelperOutputList = new ArrayList<>();
        this.queryHelperSocketList = new ArrayList<>();
        this.queryHelperInputList = new ArrayList<>();
        this.queryHelperOutputList = new ArrayList<>();

        File file = new File(this.file1);
        if(file.exists()){
            file.delete();
        }
        try {
            file.createNewFile();
            Map<String, PriorityQueue<Item>> a = new HashMap<>();

        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

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
