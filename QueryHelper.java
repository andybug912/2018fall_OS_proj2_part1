import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class QueryHelper {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public QueryHelper(){

    }

    public void start(){
        try{
            Message message = new Message("QUERY_HELPER");
            this.socket = new Socket("localhost",1234);
            this.output = new ObjectOutputStream(socket.getOutputStream());
            this.input = new ObjectInputStream(socket.getInputStream());
            output.writeObject(message);
            Message response = (Message) input.readObject();
            System.out.println(response.getTitle());
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }

        try {
            System.out.println("Please Enter if you want to disconnect");
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
            output.writeObject(new Message("DISCONNECT"));
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }
    }

    public static void main(String[] args){
        System.out.println("Please Enter to connect with TGServer");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        QueryHelper qh = new QueryHelper();
        qh.start();
    }
}
