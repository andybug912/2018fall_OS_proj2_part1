import java.io.*;
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

            this.reducer.indexLock.acquire();   // ?

            HashMap<Character, Map<String, List<InvertedIndexItem>>> comeMap = (HashMap<Character, Map<String, List<InvertedIndexItem>>>) input.readObject();
            int commingSize = comeMap.size();
            if(commingSize != this.reducer.firstFileMapNumber + this.reducer.secondFileMapNumber){
                output.writeObject("FAIL");
                return;
            }

            // get two file's name
            List<String> ss = new ArrayList<>();
            for(String s: this.reducer.myMasterIndexFiles){ ss.add(s); }
            String firstFile = ss.get(0);
            String secondFile = ss.get(1);

            // read first file's map
            FileInputStream freader = new FileInputStream(firstFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(freader);
            HashMap<Character, Map<String, PriorityQueue<InvertedIndexItem>>> map1 =
                    (HashMap<Character, Map<String, PriorityQueue<InvertedIndexItem>>>) objectInputStream.readObject();

            for(Character key: map1.keySet()){
                if(!comeMap.containsKey(key)){
                    output.writeObject("FAIL");
                    return;
                }
                Map<String, PriorityQueue<InvertedIndexItem>> thismap = map1.get(key);
                Map<String, List<InvertedIndexItem>> commingMap = comeMap.get(key);
                if( commingMap == null || commingMap.size() == 0) continue;
                for(String word: commingMap.keySet()) {
                    if(!thismap.containsKey(word)) {
                        thismap.put(word, new PriorityQueue<>(new reducerComparator()));
                        for(InvertedIndexItem iii: commingMap.get(word)){
                            thismap.get(word).offer(iii);
                        }
                    }
                    else {
                        for(InvertedIndexItem iii: commingMap.get(word)){
                            int flag = 0;
                            Iterator it = thismap.get(word).iterator();
                            while(it.hasNext()){
                                InvertedIndexItem item = (InvertedIndexItem) it.next();
                                if(item.fileID == iii.fileID){
                                    item.count += iii.count;
                                    flag = 1;
                                }
                            }
                            if(flag == 0){
                                thismap.get(word).offer(iii);
                            }
                        }
                    }
                }
            }

            FileOutputStream outStream = new FileOutputStream(firstFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStream);
            objectOutputStream.writeObject(map1);
            outStream.close();

            // read second file's map
            FileInputStream freader2 = new FileInputStream(secondFile);
            ObjectInputStream objectInputStream2 = new ObjectInputStream(freader2);
            HashMap<Character, Map<String, PriorityQueue<InvertedIndexItem>>> map2 = (HashMap<Character, Map<String, PriorityQueue<InvertedIndexItem>>>) objectInputStream2.readObject();

            for(Character key: map2.keySet()){
                if(!comeMap.containsKey(key)){
                    output.writeObject("FAIL");
                    return;
                }
                Map<String, PriorityQueue<InvertedIndexItem>> thismap2 = map2.get(key);
                Map<String, List<InvertedIndexItem>> commingMap2 = comeMap.get(key);
                if( commingMap2 == null || commingMap2.size() == 0) continue;
                for(String word: commingMap2.keySet()) {
                    if(!thismap2.containsKey(word)) {
                        thismap2.put(word, new PriorityQueue<>(new reducerComparator()));
                        for(InvertedIndexItem iii: commingMap2.get(word)){
                            thismap2.get(word).offer(iii);
                        }
                    }
                    else {
                        for(InvertedIndexItem iii: commingMap2.get(word)){
                            int flag = 0;
                            Iterator it = thismap2.get(word).iterator();
                            while(it.hasNext()){
                                InvertedIndexItem item = (InvertedIndexItem) it.next();
                                if(item.fileID == iii.fileID){
                                    item.count += iii.count;
                                    flag = 1;
                                }
                            }
                            if(flag == 0){
                                thismap2.get(word).offer(iii);
                            }
                        }
                    }
                }
            }

            FileOutputStream outStream2 = new FileOutputStream(secondFile);
            ObjectOutputStream objectOutputStream2 = new ObjectOutputStream(outStream2);
            objectOutputStream2.writeObject(map2);
            outStream2.close();

            output.writeObject("OK");

            this.reducer.indexLock.release();
        }
        catch (Exception e) {
            System.err.println("Error in reducer thread: " + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }
    }
}

class reducerComparator implements Comparator<InvertedIndexItem>, Serializable{
    @Override
    public int compare(InvertedIndexItem i1, InvertedIndexItem i2){
        if(i1.count < i2.count){
            return 1;
        }
        else if(i1.count > i2.count){
            return -1;
        }
        return 0;
    }
}