import java.io.File;
import java.util.List;

public class IndexOrder {
    public int typeOfOrder;
    public List<Integer> fileIDs;
    public List<File> files;
    public List<String> masterIndexFiles;

    public IndexOrder(List<Integer> fileIDs, List<File> files) {
        this.typeOfOrder = MasterIndexUtil.MAPPER;
        this.fileIDs = fileIDs;
        this.files = files;
    }

    public IndexOrder(List<String> masterIndexFiles) {
        this.typeOfOrder = MasterIndexUtil.REDUCER;
        this.masterIndexFiles = masterIndexFiles;
    }
}
