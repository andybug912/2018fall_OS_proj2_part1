import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class IndexingHelperThread extends Thread {
    private Socket socket;

    public IndexingHelperThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            while (true) {
                // TODO: run mapper or reducer thread
            }
        }
        catch (Exception e) {
            System.err.println("Error in helper thread: " + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }

    }
}
