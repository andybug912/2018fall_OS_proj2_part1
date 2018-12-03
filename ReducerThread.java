import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class ReducerThread extends Thread {
    private Socket socket;
    private Reducer reducer;

    public ReducerThread(Socket socket, Reducer reducer) {
        this.socket = socket;
        this.reducer = reducer;
    }

    public void run() {
        try {
            final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            Map<String, List<InvertedIndexItem>>[] comeMap = (Map<String, List<InvertedIndexItem>>[]) input.readObject();
            for(Map map:comeMap){

            }

            List<String> ss = new ArrayList<>();
            for(String s:this.reducer.myMasterIndexFiles){
                ss.add(s);
            }
            String firstFile = ss.get(0);
            String secondFile = ss.get(1);
            for(int i = 0; i <= this.reducer.firstFileMapNumber - 1; i++){
                FileInputStream freader = new FileInputStream(firstFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(freader);
                Map<String, PriorityQueue<InvertedIndexItem>> map = new HashMap<>();
                map = (Map<String, PriorityQueue<InvertedIndexItem>>) objectInputStream.readObject();

            }
            for(int i = 0; i <= this.reducer.secondFileMapNumber - 1; i++){
                File file = new File(secondFile);
            }

            this.reducer.indexLock.acquire();

            this.reducer.indexLock.release();
        }
        catch (Exception e) {
            System.err.println("Error in reducer thread: " + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }
    }
}
