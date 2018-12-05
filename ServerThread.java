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
                Message response;
                if (message.getTitle().equals("INDEX")) {
                    System.out.println("Pending to index");
                    String path = message.getPathToBeIndexed();
                    if(this.server.indexedPaths.contains(path)) {   // duplicate indexing request
                        System.out.println("Fail to index due to the path has already been indexed!");
                        response = new Message("FAIL");
                        response.setMessage("This path has already been indexed!");
                        output.writeObject(response);
                    }
                    else {
                        this.server.indexLock.acquire();
                        this.server.indexedPaths.add(path);
                        IndexingMaster indexingMaster = new IndexingMaster(this.server, path);
                        String result = indexingMaster.run();
                        this.server.indexLock.release();

                        response = new Message(result.equals("OK") ? "Successfully indexed" : result);
                        output.writeObject(response);
                    }
                }
                else if (message.getTitle().equals("QUERY")){
                    if (this.server.indexedPaths.size() == 0) {
//                        System.out.println("No path is already indexed, cannot do querying!");
                        response = new Message("No path is already indexed, cannot do querying!");
                        output.writeObject(response);
                        continue;
                    }
                    System.out.println("Pending to query");
                    this.server.queryLock.acquire();
                    while (!this.server.indexLock.tryAcquire()) {
                        Thread.sleep(60 * 1000);
                    }

                    QueryMaster queryMaster = new QueryMaster(this.server, message.getKeyWords());
                    String result = queryMaster.run();

                    this.server.queryLock.release();

                    response = new Message(result);
                    output.writeObject(response);
                }
                else if (message.getTitle().equals("DISCONNECT")) {
                    System.out.println("Disconnect from client");
                    socket.close();
                    return;
                }
                else {
                    System.out.println("Invalid message!");
                    socket.close();
                    return;
                }
            }
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
