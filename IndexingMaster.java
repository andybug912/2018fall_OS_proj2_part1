import java.io.*;
import java.util.*;

public class IndexingMaster {
    private TinyGoogleServer server;
    private String path;
    private static int fileChunkSize = 1024 * 1024; // file chunk size in byte

    public IndexingMaster(TinyGoogleServer server, String pathTOBeIndexed) {
        this.server = server;
        this.path = pathTOBeIndexed;
    }

    public String run() {
        try {
            List<ObjectOutputStream> outputs = this.server.indexingHelperOutputList;
            List<ObjectInputStream> inputs = this.server.indexingHelperInputList;

            File folder = new File(path);
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles == null || listOfFiles.length == 0) {
                return "Invalid path, no files to be indexed";
            }
            List<File> filesInChunks = splitFilesIntoChunks(listOfFiles);

            // TODO: ZHIBEN ZHU, send file chunks to helpers


            // send file chunks to helpers
            int numOfHelpers = outputs.size(), numOfFiles = filesInChunks.size();
            int helperIndex = 0, fileRangeStart = 0;
            int quotient = numOfFiles / numOfHelpers, remainder = numOfFiles % numOfHelpers;
            while (fileRangeStart < numOfFiles) {
                int fileRangeEnd = helperIndex < remainder ? fileRangeStart + quotient : fileRangeStart + quotient - 1;
                List<Integer> fileIDs = new ArrayList<>();
                for (int i = fileRangeStart; i <= fileRangeEnd; i++) {
                    int fileID = this.server.documentToID.size();
                    fileIDs.add(fileID);
                    this.server.documentToID.put(listOfFiles[i].getCanonicalPath(), fileID);
                    this.server.idToDocument.put(fileID, listOfFiles[i].getCanonicalPath());
                }
                outputs.get(helperIndex++).writeObject(
                        new IndexOrder(fileIDs, Arrays.copyOfRange(listOfFiles, fileRangeStart, fileRangeEnd))
                );
                fileRangeStart = fileRangeEnd + 1;
            }

            // receive results from helpers
            for (int i = 0; i < helperIndex; i++) {
                // TODO: receive result from helpers and update master index
                Map<String, List<InvertedIndexItem>> partialResult = (Map<String, List<InvertedIndexItem>>) inputs.get(i).readObject();
                mergeResult(partialResult);
            }


            return "OK";
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return "Error occurred when trying to do indexing";
        }
    }

    private List<File> splitFilesIntoChunks(File[] files) {
        String tempDir = "_temp";
        File temp = new File(tempDir);
        if (temp.exists()) {

        }
        temp.mkdir();
        List<File> fileChunks = new ArrayList<>();
        byte[] buffer = new byte[IndexingMaster.fileChunkSize];
        for (File file: files) {
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                if (file.length() <= IndexingMaster.fileChunkSize) {
                    File newFile = new File(tempDir, file.getName());
                    FileOutputStream out = new FileOutputStream(newFile);
                    int bytesAmount = bis.read(buffer);
                    out.write(buffer, 0, bytesAmount);
                    fileChunks.add(file);
                    continue;
                }
                int byteAmount = 0, chunkID = 0;
                while((byteAmount = bis.read(buffer)) > 0) {
                    String chunkName = String.format("%s.%03d", file.getName(), )
                }
            }
            catch (Exception e) {

            }
        }
        return fileChunks;
    }

    private void mergeResult(Map<String, List<InvertedIndexItem>> partialResult) {

    }

}
