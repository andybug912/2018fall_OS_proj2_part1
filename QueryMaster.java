import java.net.Socket;
import java.util.List;

public class QueryMaster extends Thread {
    private TinyGoogleServer server;
    private Socket socket;
    private List<String> keyWords;

    public QueryMaster(TinyGoogleServer server, Socket socket, List<String> keyWords) {
        this.server = server;
        this.socket = socket;
        this.keyWords = keyWords;
    }

    public void run() {

    }

}
