import java.io.File;
import java.util.List;

public class IndexOrder {
    public List<Integer> fileIDs;
    public File[] files;

    public IndexOrder(List<Integer> fileIDs, File[] files) {
        this.fileIDs = fileIDs;
        this.files = files;
    }
}
