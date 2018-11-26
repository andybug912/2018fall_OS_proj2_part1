import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TinyGoogleClient {
    private Message message;
    private Socket socket;
    private ObjectOutputStream output;

    public TinyGoogleClient(Message message){
        this.message = message;
    }


    public void start(){
        try{
            this.socket = new Socket("localhost",1234);
            this.output = new ObjectOutputStream(socket.getOutputStream());
            if(this.message.getTitle().equals("INDEX")){
                Message message = new Message(this.message.getTitle(),this.message.getPathToBeIndexed());
                output.writeObject(message);
            }
            else if(this.message.getTitle().equals("QUERY")){
                Message message = new Message(this.message.getTitle(),this.message.getKeyWords());
                output.writeObject(message);
            }

        }
        catch(Exception e){
            System.err.println("Error:" + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }
    }

    public static void main(String[] args){
        while(true){
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please type INDEX or QUERY:");
            String request = scanner.nextLine();
            if(request.equals("INDEX")){
                System.out.println("Please type the path to be indexed:");
                String path = scanner.nextLine();
                Message message = new Message("INDEX",path);
                TinyGoogleClient client = new TinyGoogleClient(message);
                client.start();
            }

            else if(request.equals("QUERY")){
                System.out.println("Please type the Keywords you want to query(ending with \"#\"):");
                List<String> keywords = new ArrayList<>();
                while(scanner.hasNextLine()){
                    String s = scanner.nextLine();
                    //scanner.nextLine();
                    if(!s.equals("#")) {
                        keywords.add(s);
                    }
                    else{
                        break;
                    }
                }
                Message message = new Message("QUERY",keywords);
                TinyGoogleClient client = new TinyGoogleClient(message);
                client.start();
            }
        }
    }
}
