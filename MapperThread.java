import java.io.File;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Callable;

public class MapperThread implements Callable<Boolean> {
    private List<Integer> fileIDs;
    private List<File> files;
    private List<String[]> reducerInfo;

    public MapperThread(List<Integer> fileIDs, List<File> files, List<String[]> reducerInfo) {
        this.fileIDs = fileIDs;
        this.files = files;
        this.reducerInfo = reducerInfo;
    }

    @Override
    public Boolean call() {
        try {

            return true;
        }
        catch (Exception e) {
            System.err.println("Error in mapper: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }
}
