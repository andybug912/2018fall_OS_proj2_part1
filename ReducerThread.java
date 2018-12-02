import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ReducerThread extends Thread {
    private Socket socket;
    private Reducer reducer;

    public ReducerThread(Socket socket, Reducer reducer) {
        this.socket = socket;
        this.reducer = reducer;
    }

    public void run() {
        try {
            final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            while (true) {

            }
        }
        catch (Exception e) {
            System.err.println("Error in reducer thread: " + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }
    }
}
