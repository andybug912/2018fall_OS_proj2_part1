import java.net.Socket;

public class IndexingMaster extends Thread {
    private TinyGoogleServer server;
    private Socket socket;

    public IndexingMaster(TinyGoogleServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    public void run() {

    }

}
