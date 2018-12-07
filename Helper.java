import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Helper {
    private int maxNumOfMappers;
    private int maxNumOfQueryThreads;
    private int port;
    private int helperID;

    public Helper(int helperID) {
        this.helperID = helperID;
        this.maxNumOfMappers = MasterIndexUtil.defaultMaxNumOfMappers;
        this.maxNumOfQueryThreads = MasterIndexUtil.defaultMaxNumOfQueryThreads;
    }

    public Helper(int helperID, int maxNumOfMappers, int maxNumOfQueryThreads) {
        this.helperID = helperID;
        this.maxNumOfMappers = maxNumOfMappers;
        this.maxNumOfQueryThreads = maxNumOfQueryThreads;
    }

    public void start(){
        // get helper IP
        File helperInfoFile = new File(MasterIndexUtil.helperInfoFileName);
        Scanner fileScanner;
        try{
            fileScanner = new Scanner(helperInfoFile);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return;
        }
        int i = 0;
        while(fileScanner.hasNext()){
            String[] helperInfo = fileScanner.nextLine().split(" ");
            if(i++ == this.helperID){
                this.port = Integer.parseInt(helperInfo[1]);
                break;
            }
        }
        fileScanner.close();

        // ******* wait for incoming connection from server *******
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
                final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                OrderWrapper order = (OrderWrapper) input.readObject();

                if (order.orderType == MasterIndexUtil.OrderType.INDEX) {
                    ExecutorService executor = Executors.newFixedThreadPool(this.maxNumOfMappers);
                    List<Future<Boolean>> futureList = new ArrayList<>();

                    int numOfChunks = order.fileIDs.size();
                    int numOfMappers = numOfChunks <= maxNumOfMappers ? numOfChunks : maxNumOfMappers;
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
                    ExecutorService executor = Executors.newFixedThreadPool(this.maxNumOfQueryThreads);
                    List<Future<Map<Integer, Integer>>> futureList = new ArrayList<>();
                    List<String> keyWords = order.queryKeyWords;

                    // TODO: dispatch key words to query threads
                    int numOfWords = keyWords.size();
                    int numOfQueryers = numOfWords <= maxNumOfQueryThreads ? numOfWords : maxNumOfQueryThreads;
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
//                    List<Map<Integer, Integer>> mergedResult = mergeResult(partialResults);
                    Map<Integer, Integer> mergedResult = mergeResult(partialResults);
                    output.writeObject(mergedResult);
                }
            }
            catch (Exception e) {
                System.err.println("222Error in helper: " + e.getMessage());
                e.printStackTrace(System.err);
                return;
            }
        }
    }

//    private List<Map<Integer, Integer>> mergeResult(List<Map<Integer, Integer>> partialResults) {
//        List<Map<Integer, Integer>> mergedResult = new ArrayList<>();
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

    public static void main(String[] args){
        Helper ih;
        if (args.length == 0) {
            System.out.println("input your index: ");
            Scanner scanner = new Scanner(System.in);
            ih = new Helper(Integer.parseInt(scanner.nextLine()));
        }
        else if (args.length == 1) {    // helper ID
            ih = new Helper(Integer.parseInt(args[0]));
        }
        else if (args.length == 3 ) {   // helper ID & max mapper number (indexing) & max query thread number
            ih = new Helper(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        }
        else{
            System.out.println("Invalid arguments!");
            return;
        }
        ih.start();
    }
}