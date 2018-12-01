import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class IndexingHelper {
    private int maxNumOfMappers;
    private int port;

    public IndexingHelper(int helperID) {
        this.maxNumOfMappers = MasterIndexUtil.defaultMaxNumOfMappers;
    }

    public IndexingHelper(int helperID, int maxNumOfMappers) {
        this.maxNumOfMappers = maxNumOfMappers;
    }

    public void start(){
        ServerSocket serverSocket;
        try{
            serverSocket = new ServerSocket(this.port);
        }
        catch (Exception e){
            System.err.println("111Error in helper: " + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }

        while (true) {
            try {
                Socket socket = serverSocket.accept();      //listen
                IndexingHelperThread indexingHelperThread = new IndexingHelperThread(socket);
                indexingHelperThread.start();
            }
            catch (Exception e) {
                System.err.println("222Error in helper: " + e.getMessage());
                e.printStackTrace(System.err);
                return;
            }
        }
    }

    public static void main(String[] args){
        IndexingHelper ih;
        if (args.length == 0) {
            System.out.println("input your index: ");
            Scanner scanner = new Scanner(System.in);
            ih = new IndexingHelper(Integer.parseInt(scanner.nextLine()));
        }
        else if (args.length == 1) {
            ih = new IndexingHelper(Integer.parseInt(args[0]));
        }
        else if (args.length > 1 ) {
            ih = new IndexingHelper(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        }
        else{
            System.out.println("Invalid arguments!");
            return;
        }
        ih.start();
    }
}
//
//class HelperThread extends Thread {
//    private List<Integer> fileIDs;
//    private List<File> chunks;
//
//    public HelperThread(List<Integer> fileIDs, List<File> chunks) {
//        this.fileIDs = fileIDs;
//        this.chunks = chunks;
//    }
//    public void run() {
//
//    }
//
//    private Map<String, InvertedIndexItem> genInvertedIndex() {
//        Map<String, InvertedIndexItem> map = new HashMap<>();
//        Iterator<Integer> iterator = fileIDs.iterator();
//        for (File chunk: chunks) {
//            int fileID = iterator.next();
//            // TODO:
//        }
//        return map;
//    }
//}