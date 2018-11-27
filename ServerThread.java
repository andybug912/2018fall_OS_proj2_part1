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
                    String path = message.getPathToBeIndexed();
                    if(this.server.indexedPaths.contains(path)) {   // duplicate indexing request
                        System.out.println("Fail to index due to the path has already been indexed!");
                        Message response = new Message("FAIL");
                        response.setMessage("This path has already been indexed!");
                        output.writeObject(response);
                    }
                    else {
                        this.server.indexLock.acquire();

                        this.server.indexedPaths.add(path);
                        IndexingMaster indexingMaster = new IndexingMaster(this.server, this.socket, path);
                        indexingMaster.run();
                        indexingMaster.join();

                        this.server.indexLock.release();
                    }
                }
                else if (message.getTitle().equals("QUERY")){
                    System.out.println("Pending to query");
                    this.server.queryLock.acquire();

                    QueryMaster queryMaster = new QueryMaster(this.server, this.socket, message.getKeyWords());
                    queryMaster.run();
                    queryMaster.join();

                    this.server.queryLock.release();
                }
                else if (message.getTitle().equals("DISCONNECT")) {
                    System.out.println("Disconnect from client");
                    socket.close();
                    return;
                }
                else if (message.getTitle().equals("INDEX_HELPER")) {
                    this.server.indexingHelperSocketList.add(socket);
                    this.server.indexingHelperInputList.add(input);
                    this.server.indexingHelperOutputList.add(output);
                    Message reply = new Message("CONNECTED");
                    output.writeObject(reply);
                }
                else if (message.getTitle().equals("QUERY_HELPER")) {
                    this.server.queryHelperSocketList.add(socket);
                    this.server.queryHelperInputList.add(input);
                    this.server.queryHelperOutputList.add(output);
                    Message reply = new Message("CONNECTED");
                    output.writeObject(reply);
                }
            }
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
