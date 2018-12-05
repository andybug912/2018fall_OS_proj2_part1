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
    public Map<Integer, Integer> call() throws Exception {
        Map<Integer, Integer> result = new HashMap<>();
        for (String keyWord: this.keyWords) {
            char firstLetter = keyWord.charAt(0);
            String fileName = MasterIndexUtil.findMasterIndexFileName(firstLetter);
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Map<Character, Map<String, PriorityQueue<InvertedIndexItem>>> allMaps =
                    (Map<Character, Map<String, PriorityQueue<InvertedIndexItem>>>) ois.readObject();
            if (allMaps.get(firstLetter).containsKey(keyWord)) {
                PriorityQueue<InvertedIndexItem> pq = allMaps.get(firstLetter).get(keyWord);
                for (InvertedIndexItem item: pq) {
                    result.put(item.fileID, result.getOrDefault(item.fileID, 0) + item.count);
                }
            }
            ois.close();
            fis.close();
        }
        return result.isEmpty() ? null : result;
    }
}
