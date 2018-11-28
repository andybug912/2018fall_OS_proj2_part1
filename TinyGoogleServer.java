import java.io.*;
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
    final public String masterIndexPath= "MasterIndex/";
    private String[] filelist = {masterIndexPath+"ab.txt",masterIndexPath+"cdefg.txt",masterIndexPath+"hijkl.txt",masterIndexPath+"mnopq.txt",masterIndexPath+"t.txt",masterIndexPath+"rsuvwxyz.txt"};
    private int[] mapNumber = {2,5,5,5,1,8};
    public Set<String> indexedPaths;
    public Semaphore indexLock;
    public Semaphore queryLock;
    public Map<Integer, String> idToDocument;
    public Map<String, Integer> documentToID;

    public TinyGoogleServer() {
        this.indexedPaths = new HashSet<>();
        this.indexingHelperSocketList = new ArrayList<>();
        this.indexingHelperInputList = new ArrayList<>();
        this.indexingHelperOutputList = new ArrayList<>();
        this.queryHelperSocketList = new ArrayList<>();
        this.queryHelperInputList = new ArrayList<>();
        this.queryHelperOutputList = new ArrayList<>();

        //put map into file
        File a = new File(masterIndexPath);
        if(!a.exists()){
            a.mkdir();
        }
        for(int i=0;i<=5;i++){
            File file = new File(this.filelist[i]);
            if(file.exists()){
                continue;
            }
            try {
                file.createNewFile();
                int mapnumber = mapNumber[i];
                for(int k=0;k<=mapnumber-1;k++){
                    Map<String, PriorityQueue<InvertedIndexItem>> map = new HashMap<>();
                    FileOutputStream outputStream = new FileOutputStream(this.filelist[i]);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(map);
                    outputStream.close();
                }
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }
        }

        this.indexLock = new Semaphore(1, true);
        this.queryLock = new Semaphore(1, true);
        this.idToDocument = new HashMap<>();
        this.documentToID = new HashMap<>();
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
