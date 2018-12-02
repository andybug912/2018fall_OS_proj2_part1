import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class IndexingMaster {
    private TinyGoogleServer server;
    private String path;
    private static int fileChunkSize = 1024 * 1024; // file chunk size in byte
    private static int maxRowOfChunk = 20000;   // file chunk size in row

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
            List<File> tempFilesInChunks = new ArrayList<>(Arrays.asList(new File[filesInChunks.size()]));
            Collections.copy(tempFilesInChunks,filesInChunks);

            // send file chunks to helpers
            int numOfHelpers = outputs.size(), numOfFiles = filesInChunks.size();
            int helperIndex = 0, fileRangeStart = 0;
//            int quotient = numOfFiles / numOfHelpers, remainder = numOfFiles % numOfHelpers;
            int quotient = numOfFiles / 4, remainder = numOfFiles % 4;
            while (fileRangeStart < numOfFiles) {
                int fileRangeEnd = helperIndex < remainder ? fileRangeStart + quotient : fileRangeStart + quotient - 1;
                List<Integer> fileIDs = new ArrayList<>();
                for (int i = fileRangeStart; i <= fileRangeEnd; i++) {
                    int fileID = this.server.idToDocument.size();
                    if(!this.server.documentToID.containsKey(
                            tempFilesInChunks.get(i).getCanonicalPath().substring(0,tempFilesInChunks.get(i).getCanonicalPath().length()-7)
                        )
                    ){
                        fileIDs.add(fileID);
                        this.server.documentToID.put(tempFilesInChunks.get(i).getCanonicalPath().substring(0,tempFilesInChunks.get(i).getCanonicalPath().length()-7),fileID);
                        this.server.idToDocument.put(fileID, tempFilesInChunks.get(i).getCanonicalPath().substring(0,tempFilesInChunks.get(i).getCanonicalPath().length()-7));
                    }
                    else{
                        fileID = this.server.documentToID.get(tempFilesInChunks.get(i).getCanonicalPath().substring(0,tempFilesInChunks.get(i).getCanonicalPath().length()-7));
                        fileIDs.add(fileID);
                    }
                }
                System.out.println(fileIDs);
                System.out.println(tempFilesInChunks.subList(fileRangeStart, fileRangeEnd + 1));
//                outputs.get(helperIndex++).writeObject(
////                        new IndexOrder(fileIDs, Arrays.copyOfRange(listOfFiles, fileRangeStart, fileRangeEnd))
//                        new IndexOrder(fileIDs, tempFilesInChunks.subList(fileRangeStart, fileRangeEnd + 1))
//                );
                fileRangeStart = fileRangeEnd + 1;
            }
//
//            // receive results from helpers
//            for (int i = 0; i < helperIndex; i++) {
//                // TODO: receive result from helpers and update master index
//                Map<String, List<InvertedIndexItem>> partialResult = (Map<String, List<InvertedIndexItem>>) inputs.get(i).readObject();
//                mergeResult(partialResult);
//            }


            return "OK";
        }
        catch (Exception e) {
            System.err.println("111Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return "Error occurred when trying to do indexing";
        }
    }

    private List<File> splitFilesIntoChunks(File[] files) {
        List<File> fileChunks = new ArrayList<>();
        try {
//            String tempDir = this.path + "/_temp/";
//            System.out.println(tempDir);
//            File temp = new File(tempDir);
//            temp.mkdir();
            for (File file: files) {
                List<File> tempList = splitFile(file, IndexingMaster.maxRowOfChunk);
                fileChunks.addAll(tempList);
            }
        }
        catch (Exception e) {
            System.err.println("222Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
        return fileChunks;
    }

    public List<File> splitFile(File bigFile, int maxRows) throws IOException {
        List<File> files = new ArrayList<>();
        Path bigFilePath = bigFile.toPath();
        MappedByteBuffer bb;
        try(FileChannel in = FileChannel.open(bigFilePath, StandardOpenOption.READ)) {
            bb=in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
        }
        for(int start=0, pos=0, end=bb.remaining(), i=1, lineNum=1; pos<end; lineNum++) {
            while(pos<end && bb.get(pos++)!='\n');
            if(lineNum < maxRows && pos<end) continue;
            Path splitFile = Paths.get(this.path + "/" + bigFile.getName() + ".chunk" + i++);
            System.out.println(splitFile.toFile().getName());
            try(FileChannel out = FileChannel.open(splitFile, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                bb.position(start).limit(pos);
                while(bb.hasRemaining()) out.write(bb);
                bb.clear();
                start=pos;
                lineNum = 0;
            }
            files.add(splitFile.toFile());
        }
        return files;
    }

    private void mergeResult(Map<String, List<InvertedIndexItem>> partialResult) {

    }

}
