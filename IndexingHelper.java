import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

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
                IndexingHelperThread indexingHelperThread = new IndexingHelperThread(socket);
                indexingHelperThread.start();
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
        else if (args.length == 1) {
            ih = new IndexingHelper(Integer.parseInt(args[0]));
        }
        else if (args.length > 1 ) {
            ih = new IndexingHelper(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        }
        else{
            System.out.println("Invalid arguments!");
            return;
        }
        ih.start();
    }
}