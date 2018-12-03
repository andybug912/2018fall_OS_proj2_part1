import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Semaphore;

public class TinyGoogleServer {

    public Set<String> indexedPaths;
    public Semaphore indexLock;
    public Semaphore queryLock;
    public Map<Integer, String> idToDocument;
    public Map<String, Integer> documentToID;
    public ArrayList<String[]> reducerInfo;
    public ArrayList<String[]> helperInfo;

    public TinyGoogleServer() {
        // read helper info, ip & port
        File helperInfoFile = new File(MasterIndexUtil.helperInfoFileName);
        Scanner fileScanner;
        try{
            fileScanner = new Scanner(helperInfoFile);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return;
        }
        this.helperInfo = new ArrayList<>();
        while(fileScanner.hasNext()){
            String[] info = fileScanner.nextLine().split(" ");
            helperInfo.add(info);
        }
        fileScanner.close();

        // read reducer info, ip & port
        File reducerInfoFile = new File(MasterIndexUtil.reducerInfoFileName);
        try {
            fileScanner = new Scanner(reducerInfoFile);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return;
        }
        reducerInfo = new ArrayList<>();
        while(fileScanner.hasNext()){
            String[] info = fileScanner.nextLine().split(" ");
            this.reducerInfo.add(Arrays.copyOfRange(info, 0, 3));
        }
        fileScanner.close();

        // put map into master index file
        File a = new File(MasterIndexUtil.masterIndexPath);
        if(!a.exists()){
            a.mkdir();
        }
        for(int i = 0; i < MasterIndexUtil.filelist.length; i++){
            File file = new File(MasterIndexUtil.filelist[i]);
            if(file.exists()){
                continue;
            }
            try {
                file.createNewFile();
                int mapNumber = MasterIndexUtil.mapNumber[i];
                String filename = MasterIndexUtil.filelist[i].substring(13,MasterIndexUtil.filelist[i].length()-4);
                System.out.println(filename);
                for(int k=0;k<=mapNumber-1;k++){
                    LinkedHashMap<Character,Map<String, PriorityQueue<InvertedIndexItem>>> map = new LinkedHashMap<>();
//                    Map<String, PriorityQueue<InvertedIndexItem>> map = new HashMap<>();
                    FileOutputStream outputStream = new FileOutputStream(MasterIndexUtil.filelist[i]);
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
        this.indexedPaths = new HashSet<>();
    }

    public void run(int port) {
        // **** setup server socket ****
        final ServerSocket serverSock;
        try
        {
            serverSock = new ServerSocket(port);
            System.out.println("***** Tiny Google Server Is Up *****");
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
        if (args.length == 0) {
            server.run(1234);
        }
        else if (args.length == 1){
            try {
                server.run(Integer.parseInt(args[0]));
            }
            catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }
        else {
            System.out.println("Wrong input arguments!");
        }
    }
}
