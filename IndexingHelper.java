import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class IndexingHelper {
    private int maxNumOfMappers;
    private int port;
    private int helperID;

    public IndexingHelper(int helperID) {
        this.helperID = helperID;
        this.maxNumOfMappers = MasterIndexUtil.defaultMaxNumOfMappers;
    }

    public IndexingHelper(int helperID, int maxNumOfMappers) {
        this.helperID = helperID;
        this.maxNumOfMappers = maxNumOfMappers;
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

                IndexOrder order = (IndexOrder) input.readObject();
                // TODO: dispatch order to mappers
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
        else if (args.length == 1) {    // helper ID
            ih = new IndexingHelper(Integer.parseInt(args[0]));
        }
        else if (args.length == 2 ) {   // helper ID & max mapper number
            ih = new IndexingHelper(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        }
        else{
            System.out.println("Invalid arguments!");
            return;
        }
        ih.start();
    }
}