import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndexingMaster extends Thread {
    private TinyGoogleServer server;
    private Socket socket;
    private String path;

    public IndexingMaster(TinyGoogleServer server, Socket socket, String pathTOBeIndexed) {
        this.server = server;
        this.socket = socket;
        this.path = pathTOBeIndexed;
    }

    public void run() {
        try {
            File folder = new File(path);
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles == null || listOfFiles.length == 0) {
                // TODO: error to client
                return;
            }
            int numOfFiles = listOfFiles.length;
            // TODO: modify the output & input
            List<IndexingHelper> output = this.server.indexingHelperList;
            int numOfHelpers = output.size();
            int helperIndex = 0, fileRangeStart = 0;
            while (fileRangeStart < numOfFiles) {
                int fileRangeEnd = Math.min(fileRangeStart + numOfFiles / numOfHelpers, numOfFiles);
                List<Integer> fileIDs = new ArrayList<>();
                for (int i = fileRangeStart; i <= fileRangeEnd; i++) {
                    int fileID = this.server.documentToID.size();
                    fileIDs.add(fileID);
                    this.server.documentToID.put(listOfFiles[i].getName(), fileID);
                    this.server.idToDocument.put(fileID, listOfFiles[i].getName());
                }
                output.get(helperIndex++).writeObject(Arrays.copyOfRange(listOfFiles, fileRangeStart, fileRangeEnd));
                output.get(helperIndex++).writeObject(fileIDs);
                fileRangeStart = fileRangeEnd;
            }
            for (int i = 0; i < helperIndex; i++) {
                // TODO: receive result from helpers and update master index
            }
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            // TODO: output fail msg to client
        }
    }

}
