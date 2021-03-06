import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.concurrent.Callable;

public class QueryThread implements Callable<Map<Integer, Integer>> {
    private List<String> keyWords;
    private List<InvertedIndexItem> result;

    public QueryThread(List<String> keyWords) {
        this.keyWords = keyWords;
        this.result = new ArrayList<>();
    }

    @Override
    public Map<Integer, Integer> call() {
        Map<Integer, Integer> result = new HashMap<>();
        for (String keyWord: this.keyWords) {
            char firstLetter = keyWord.charAt(0);
            String fileName = MasterIndexUtil.findMasterIndexFileName(firstLetter);
            try {
                FileInputStream fis = new FileInputStream(fileName);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Map<Character, Map<String, List<InvertedIndexItem>>> allMaps =
                        (Map<Character, Map<String, List<InvertedIndexItem>>>) ois.readObject();
                if (allMaps.get(firstLetter).containsKey(keyWord)) {
                    List<InvertedIndexItem> pq = allMaps.get(firstLetter).get(keyWord);
                    for (InvertedIndexItem item: pq) {
                        if (result.containsKey(item.fileID)) {
                            result.put(item.fileID, result.get(item.fileID) + item.count);
                        }
                        else {
                            result.put(item.fileID, item.count);
                        }
                    }
                }
                ois.close();
                fis.close();
            }
            catch (Exception e) {
                System.err.println("Error in query thread: " + e.getMessage());
                e.printStackTrace(System.err);
                return null;
            }
        }
        return result.isEmpty() ? null : result;
    }
}
