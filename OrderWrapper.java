import java.io.File;
import java.io.Serializable;
import java.util.List;

public class OrderWrapper implements Serializable {
    public MasterIndexUtil.OrderType orderType;

    public List<Integer> fileIDs;
    public List<File> files;
    public List<String[]> reducerInfo;

    public List<String> queryKeyWords;

    public OrderWrapper(List<Integer> fileIDs, List<File> files, List<String[]> reducerInfo) {
        this.orderType = MasterIndexUtil.OrderType.INDEX;
        this.fileIDs = fileIDs;
        this.files = files;
        this.reducerInfo = reducerInfo;
    }

    public OrderWrapper(List<String> queryKeyWords) {
        this.orderType = MasterIndexUtil.OrderType.QUERY;
        this.queryKeyWords = queryKeyWords;
    }
}
