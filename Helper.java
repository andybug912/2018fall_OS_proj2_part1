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
    public int maxNumOfMappers;
    public int maxNumOfQueryThreads;
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
                HelperThread helperThread = new HelperThread(this, socket);
                helperThread.run();
            }
            catch (Exception e) {
                System.err.println("222Error in helper: " + e.getMessage());
                e.printStackTrace(System.err);
                return;
            }
        }
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