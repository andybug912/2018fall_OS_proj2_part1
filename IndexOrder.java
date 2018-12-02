import java.io.File;
import java.util.List;

public class IndexOrder {
    public List<Integer> fileIDs;
    public List<File> files;
    public List<String> masterIndexFiles;

    public IndexOrder(List<Integer> fileIDs, List<File> files) {
        this.fileIDs = fileIDs;
        this.files = files;
    }
}
