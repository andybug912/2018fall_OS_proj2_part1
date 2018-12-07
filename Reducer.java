import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class Reducer {
    private int port;
    public Set<String> myMasterIndexFiles;
    private String validFirstLetters;
    public int firstFileMapNumber;
    public int secondFileMapNumber;
    public Semaphore reducerIndexLock;

    public Reducer(int reducerID) {
        myMasterIndexFiles = new HashSet<>();

        // get reducer IP & masterIndex files to manage
        File reducerInfoFile = new File(MasterIndexUtil.reducerInfoFileName);
        Scanner fileScanner;
        try{
            fileScanner = new Scanner(reducerInfoFile);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return;
        }
        int i = 0;
        while(fileScanner.hasNext()){
            String[] reducerInfo = fileScanner.nextLine().split(" ");
            if(i++ == reducerID){
                this.port = Integer.parseInt(reducerInfo[1]);
                this.validFirstLetters = reducerInfo[2];
                this.myMasterIndexFiles.add(MasterIndexUtil.filelist[Integer.parseInt(reducerInfo[3])]);
                this.myMasterIndexFiles.add(MasterIndexUtil.filelist[Integer.parseInt(reducerInfo[4])]);
                this.firstFileMapNumber = MasterIndexUtil.mapNumber[Integer.parseInt(reducerInfo[3])];
                this.secondFileMapNumber = MasterIndexUtil.mapNumber[Integer.parseInt(reducerInfo[4])];
                break;
            }
        }
        fileScanner.close();

        this.reducerIndexLock = new Semaphore(1, true);
    }

    public void run() {

        ServerSocket serverSocket;
        try{
            serverSocket = new ServerSocket(this.port);
        }
        catch (Exception e){
            System.err.println("111Error in reducer: " + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }

        while (true) {
            try {
                Socket socket = serverSocket.accept();      //listen
                ReducerThread reducerThread = new ReducerThread(socket, this);
                reducerThread.start();
            }
            catch (Exception e) {
                System.err.println("222Error in reducer: " + e.getMessage());
                e.printStackTrace(System.err);
                return;
            }
        }
    }

    public static void main(String[] args) {
        Reducer reducer;
        if (args.length == 0) {
            System.out.println("input your index: ");
            Scanner scanner = new Scanner(System.in);
            reducer = new Reducer(Integer.parseInt(scanner.nextLine()));
        }
        else if (args.length == 1) {
            reducer = new Reducer(Integer.parseInt(args[0]));
        }
        else {
            System.out.println("Wrong arguments!");
            return;
        }
        reducer.run();
    }
}
