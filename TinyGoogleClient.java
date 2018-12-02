import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TinyGoogleClient {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    public void start(){
        while (true) {
            try{
                // connect to server
                this.socket = new Socket("localhost",1234);
                this.output = new ObjectOutputStream(socket.getOutputStream());
                this.input = new ObjectInputStream(socket.getInputStream());

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
                    while(scanner.hasNextLine()){
                        String s = scanner.nextLine();
                        //scanner.nextLine();
                        if(!s.equals("#")) {
                            keyWords.add(s);
                        }
                        else{
                            break;
                        }
                    }
                    message = new Message("QUERY", keyWords);
                    output.writeObject(message);
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
        TinyGoogleClient client = new TinyGoogleClient();
        client.start();
    }
}
