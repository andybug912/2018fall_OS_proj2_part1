import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class IndexingHelper {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private int maxNumOfMappers = 3;

    public IndexingHelper() {}

    public IndexingHelper(int maxNumOfMappers) {
        this.maxNumOfMappers = maxNumOfMappers;
    }

    public void start(){
        try{
            this.socket = new Socket("localhost",1234);
            this.output = new ObjectOutputStream(socket.getOutputStream());
            this.input = new ObjectInputStream(socket.getInputStream());

            Message message = new Message("INDEX_HELPER");
            output.writeObject(message);

            while (true) {
                IndexOrder task = (IndexOrder) input.readObject();
//                Map<String, InvertedIndexItem> map = genInvertedIndex(task);
//                output.writeObject(map);
//                HelperThread ht1 = new HelperThread();
            }
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }
    }

    public static void main(String[] args){
        IndexingHelper ih;
        if (args.length == 1) {
            ih = new IndexingHelper(Integer.parseInt(args[0]));
        }
//        else if (args.length > 1 ) {
//
//        }
        else{
            ih = new IndexingHelper();
        }
        ih.start();
    }
}

class HelperThread extends Thread {
    private List<Integer> fileIDs;
    private List<File> chunks;

    public HelperThread(List<Integer> fileIDs, List<File> chunks) {
        this.fileIDs = fileIDs;
        this.chunks = chunks;
    }
    public void run() {

    }

    private Map<String, InvertedIndexItem> genInvertedIndex() {
        Map<String, InvertedIndexItem> map = new HashMap<>();
        Iterator<Integer> iterator = fileIDs.iterator();
        for (File chunk: chunks) {
            int fileID = iterator.next();
            // TODO:
        }
        return map;
    }
}