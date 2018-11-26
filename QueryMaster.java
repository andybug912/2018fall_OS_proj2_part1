import java.net.Socket;

public class QueryMaster extends Thread {
    private TinyGoogleServer server;
    private Socket socket;

    public QueryMaster(TinyGoogleServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    public void run() {

    }

}
