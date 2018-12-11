import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HelperThread {
    private Helper helper;
    private Socket socket;

    public HelperThread(Helper helper, Socket socket) {
        this.helper = helper;
        this.socket = socket;
    }

    public void run() {
        try {
            final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            OrderWrapper order = (OrderWrapper) input.readObject();

            if (order.orderType == MasterIndexUtil.OrderType.INDEX) {
                ExecutorService executor = Executors.newFixedThreadPool(this.helper.maxNumOfMappers);
                List<Future<Boolean>> futureList = new ArrayList<>();

                int numOfChunks = order.fileIDs.size();
                int numOfMappers = numOfChunks <= this.helper.maxNumOfMappers ? numOfChunks : this.helper.maxNumOfMappers;
                int mapperIndex = 0, fileRangeStart = 0;
                int quotient = numOfChunks / numOfMappers , remainder = numOfChunks % numOfMappers;
                while(fileRangeStart < numOfChunks){
                    int fileRangeEnd = mapperIndex < remainder ? fileRangeStart + quotient : fileRangeStart + quotient - 1;
                    MapperThread mapperThread = new MapperThread(
                            order.fileIDs.subList(fileRangeStart, fileRangeEnd + 1),
                            new ArrayList<File>(order.files.subList(fileRangeStart, fileRangeEnd + 1)),
                            order.reducerInfo);
                    Future<Boolean> future = executor.submit(mapperThread);
                    futureList.add(future);
                    mapperIndex++;
                    fileRangeStart = fileRangeEnd + 1;
                }

                for (Future<Boolean> _future: futureList) {
                    if (!_future.get()) {
                        output.writeObject("FAIL");
                        System.out.println("At least one mapper failed!");
                        socket.close();
                        return;
                    }
                }
                output.writeObject("OK");
            }
            else if (order.orderType == MasterIndexUtil.OrderType.QUERY) {
                ExecutorService executor = Executors.newFixedThreadPool(this.helper.maxNumOfQueryThreads);
                List<Future<Map<Integer, Integer>>> futureList = new ArrayList<>();
                List<String> keyWords = order.queryKeyWords;

                // dispatch key words to query threads
                int numOfWords = keyWords.size();
                int numOfQueryers = numOfWords <= this.helper.maxNumOfQueryThreads ? numOfWords : this.helper.maxNumOfQueryThreads;
                int queryIndex = 0, wordRangeStart = 0;
                int quotient = numOfWords / numOfQueryers, remainder = numOfWords % numOfQueryers;

                while(wordRangeStart < numOfWords){
                    int wordRangeEnd = queryIndex < remainder ? wordRangeStart + quotient : wordRangeStart + quotient - 1;
                    QueryThread queryThread = new QueryThread(
                            new ArrayList<String>(keyWords.subList(wordRangeStart, wordRangeEnd + 1))
                    );
                    Future<Map<Integer, Integer>> future = executor.submit(queryThread);
                    futureList.add(future);
                    queryIndex++;
                    wordRangeStart = wordRangeEnd + 1;
                }

                List<Map<Integer, Integer>> partialResults = new ArrayList<>(1);
                for (Future<Map<Integer, Integer>> _future: futureList) {
                    Map<Integer, Integer> temp = _future.get();
                    if (temp != null) {
                        partialResults.add(temp);
                    }
                }
                Map<Integer, Integer> mergedResult = mergeResult(partialResults);
                output.writeObject(mergedResult);
            }
            else {
                socket.close();
            }
        }
        catch (Exception e) {
            System.err.println("Error in helper thread: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private Map<Integer, Integer> mergeResult(List<Map<Integer, Integer>> partialResults){
        Map<Integer, Integer> mergedResult = new HashMap<>();
        for(Map<Integer, Integer> map: partialResults){
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
        return mergedResult;
    }
}
