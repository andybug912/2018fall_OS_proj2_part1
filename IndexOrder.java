import java.io.File;
import java.util.List;

public class IndexOrder {
    public List<Integer> fileIDs;
    public List<File> files;
    public List<String[]> reducerInfo;

    public IndexOrder(List<Integer> fileIDs, List<File> files, List<String[]> reducerInfo) {
        this.fileIDs = fileIDs;
        this.files = files;
        this.reducerInfo = reducerInfo;
    }
}
