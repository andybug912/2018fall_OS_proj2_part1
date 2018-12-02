import java.io.File;
import java.util.List;

public class IndexOrder {
    public List<Integer> fileIDs;
    public List<File> files;//改

    public IndexOrder(List<Integer> fileIDs, List<File> files) {
        this.fileIDs = fileIDs;
        this.files = files;
    }
}
