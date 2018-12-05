import java.io.*;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class IndexingMaster {
    private TinyGoogleServer server;
    private String path;

    public IndexingMaster(TinyGoogleServer server, String pathTOBeIndexed) {
        this.server = server;
        if (pathTOBeIndexed.charAt(pathTOBeIndexed.length() - 1) == '/') {
            pathTOBeIndexed = pathTOBeIndexed.substring(0, pathTOBeIndexed.length() - 1);
        }
        this.path = pathTOBeIndexed;
    }

    public String run() {
        try {
            // split files into chunks
            File folder = new File(path);
            if (folder.isFile()) {
                return "This is a file, not a PATH!";
            }
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles == null || listOfFiles.length == 0) {
                return "Invalid path, no files to be indexed";
            }
            List<File> filesInChunks = splitFilesIntoChunks(listOfFiles);

            // send file chunks to helpers
            List<File> tempFilesInChunks = new ArrayList<>(Arrays.asList(new File[filesInChunks.size()]));
            Collections.copy(tempFilesInChunks,filesInChunks);
            int numOfHelpers = this.server.helperInfo.size(), numOfFiles = filesInChunks.size();
            int helperIndex = 0, fileRangeStart = 0;
            int quotient = numOfFiles / numOfHelpers, remainder = numOfFiles % numOfHelpers;

            List<ObjectOutputStream> outputList = new ArrayList<>();
            List<ObjectInputStream> inputList = new ArrayList<>();
            for (String[] info: this.server.helperInfo) {
                    Socket socket = new Socket(info[0], Integer.parseInt(info[1]));
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    outputList.add(output);
                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                    inputList.add(input);
            }

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
                outputList.get(helperIndex++).writeObject(
                    new OrderWrapper(fileIDs, new ArrayList<File>(tempFilesInChunks.subList(fileRangeStart, fileRangeEnd + 1)), this.server.reducerInfo)
                );
                fileRangeStart = fileRangeEnd + 1;
            }

            for(ObjectInputStream inputStream:inputList){
                String response = (String) inputStream.readObject();
                if(response.equals("FAIL")){
                    deleteTempChunks();
                    return "FAIL";
                }
            }
            deleteTempChunks();
            return "OK";
        }
        catch (Exception e) {
            System.err.println("111Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return "Error occurred when trying to do indexing";
        }
    }

    private void deleteTempChunks() {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        for (File chunk: listOfFiles) {
            if (chunk.getName().contains(".chunk")) {
                chunk.delete();
            }
        }
    }

    private List<File> splitFilesIntoChunks(File[] files) {
        List<File> fileChunks = new ArrayList<>();
        try {
            for (File file: files) {
                List<File> tempList = splitFile(file, MasterIndexUtil.maxRowOfChunk);
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
}
