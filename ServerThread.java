import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerThread extends Thread {
    private Socket socket;
    private TinyGoogleServer server;

    public ServerThread(Socket socket, TinyGoogleServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            while(true) {
                Message message = (Message) input.readObject();
                if (message.getTitle().equals("INDEX")) {
                    System.out.println("Pending to index");
                }
                else if (message.getTitle().equals("QUERY")){
                    System.out.println("Pending to query");
                    System.out.println(message.getKeyWords());
                }
                else if (message.getTitle().equals("DISCONNECT")) {
                    System.out.println("Disconnect from client");
                    socket.close();
                    return;
                }
                else if (message.getTitle().equals("INDEX_HELPER")) {

                }
                else if (message.getTitle().equals("QUERY_HELPER")) {
                    
                }
            }
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
