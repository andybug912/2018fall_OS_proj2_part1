import java.io.Serializable;

public class InvertedIndexItem implements Serializable {
    public int fileID;
    public int count;

    public InvertedIndexItem(int fileID, int count) {
        this.fileID = fileID;
        this.count = count;
    }
}
