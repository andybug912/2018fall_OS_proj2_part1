import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.*;

public class QueryMaster {
    private TinyGoogleServer server;
    private List<String> keyWords;

    public QueryMaster(TinyGoogleServer server, List<String> keyWords) {
        this.server = server;
        this.keyWords = keyWords;
    }

    public List<InvertedIndexItem> run() {
        try {
            int numOfHelpers = this.server.helperInfo.size() , numOfWords = keyWords.size();
            int helperIndex = 0, wordRangeStart = 0;
            int quotient = numOfWords / numOfHelpers, remainder = numOfWords % numOfHelpers;

            List<ObjectOutputStream> outputList = new ArrayList<>();
            List<ObjectInputStream> inputList = new ArrayList<>();
            List<Socket> socketList = new ArrayList<>();
            for (String[] info: this.server.helperInfo) {
                Socket socket = new Socket(info[0], Integer.parseInt(info[1]));
                socketList.add(socket);
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                outputList.add(output);
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                inputList.add(input);
            }

            while (wordRangeStart < numOfWords) {
                int wordRangeEnd = helperIndex < remainder ? wordRangeStart + quotient : wordRangeStart + quotient - 1;
                outputList.get(helperIndex++).writeObject(
                        new OrderWrapper(new ArrayList<String>(keyWords.subList(wordRangeStart, wordRangeEnd + 1)))
                );
                wordRangeStart = wordRangeEnd + 1;
            }

            // merge
            Map<Integer, Integer> mergedResult =  new HashMap<>();
            for (int i = 0; i < helperIndex; i++) {
                Map<Integer, Integer> map = (Map<Integer, Integer>) inputList.get(i).readObject();
                Iterator it = map.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry pair = (Map.Entry) it.next();
                    Integer fileID = (Integer) pair.getKey();
                    Integer score = (Integer) pair.getValue();
                    if(!mergedResult.containsKey(fileID)){
                        mergedResult.put(fileID, score);
                    }
                    else{
                        mergedResult.put(fileID, mergedResult.get(fileID) + score);
                    }
                }
            }

            List<InvertedIndexItem> pq = new ArrayList<>();
            Iterator it2 = mergedResult.entrySet().iterator();
            while(it2.hasNext()){
                Map.Entry pair = (Map.Entry) it2.next();
                Integer fileID = (Integer) pair.getKey();
                Integer score = (Integer) pair.getValue();
                pq.add(new InvertedIndexItem(fileID, score));
            }

            for (int i = 0; i < socketList.size(); i++) {
                OrderWrapper closeOrder = new OrderWrapper();
                outputList.get(i).writeObject(closeOrder);
                socketList.get(i).close();
            }

            return pq;
        }
        catch (Exception e){
            System.err.println("111Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return null;
        }
    }

}