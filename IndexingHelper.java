import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class IndexingHelper {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public IndexingHelper(){

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
                Map<String, List<InvertedIndexItem>> map = genInvertedIndex(task);
                output.writeObject(map);
            }
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }
//
//        try {
//            System.out.println("Please Enter if you want to disconnect");
//            Scanner scanner = new Scanner(System.in);
//            scanner.nextLine();
//            output.writeObject(new Message("DISCONNECT"));
//        }
//        catch (Exception e) {
//
//        }
    }

    private Map<String, List<InvertedIndexItem>> genInvertedIndex(IndexOrder task) {
        Map<String, List<InvertedIndexItem>> map = new HashMap<>();
        Iterator<Integer> iterator = task.fileIDs.iterator();
        for (File file: task.files) {
            int fileID = iterator.next();
            // TODO:
        }
        return map;
    }

//    private void wordCount

    public static void main(String[] args){
        IndexingHelper ih = new IndexingHelper();
        ih.start();
    }
}
