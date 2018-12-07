import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

public class TinyGoogleClient {
    private String serverIP;
    private int serverPort;

    public TinyGoogleClient(String ip, int port) {
        this.serverIP = ip;
        this.serverPort = port;
    }

    public void start(){
        while (true) {
            try{
                // connect to server
                Socket socket = new Socket(this.serverIP,this.serverPort);
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                // index or query
                Scanner scanner = new Scanner(System.in);
                System.out.println("1 for INDEX or 2 for QUERY:");
                String request = scanner.nextLine();

                Message message, response;
                if(request.equals("1")){    // index
                    System.out.println("Please type the path to be indexed:");
                    String path = scanner.nextLine();
                    message = new Message("INDEX", path);
                    output.writeObject(message);

                    response = (Message) input.readObject();
                    System.out.println(response.getTitle());
                }
                else if(request.equals("2")){   // query
                    List<String> keyWords = new ArrayList<>();
                    System.out.println("Please input the words you want to query(ending with #): ");
                    while(scanner.hasNextLine()){
                        String s = scanner.nextLine();
                        if(!s.equals("#")) {
                            keyWords.add(s);
                        }
                        else{
                            break;
                        }
                    }
                    message = new Message("QUERY", keyWords);
                    output.writeObject(message);

                    response = (Message) input.readObject();
                    if(response.getTitle().equals("OK")){
                        List<String> queryResult = response.getQueryResult();
                        for(String s: queryResult){
                            System.out.println(s);
                        }
                    }
                    else{
                        System.out.println(response.getTitle());
                    }
                }
            }
            catch(Exception e){
                System.err.println("Error:" + e.getMessage());
                e.printStackTrace(System.err);
                return;
            }
        }
    }

    public static void main(String[] args){
        TinyGoogleClient client;
        if (args.length == 0) {
            client = new TinyGoogleClient("localhost", 1234);
        }
        else if (args.length == 2) {
            client = new TinyGoogleClient(args[0], Integer.parseInt(args[1]));
        }
        else {
            System.out.println("Wrong arguments!");
            return;
        }
        client.start();
    }
}
