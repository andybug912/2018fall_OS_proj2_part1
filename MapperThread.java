import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class MapperThread implements Callable<Boolean> {
    private List<Integer> fileIDs;
    private List<File> files;
    private List<String[]> reducerInfo;
    Map<String, List<InvertedIndexItem>>[] mapArray;

    public MapperThread(List<Integer> fileIDs, List<File> files, List<String[]> reducerInfo) {
        this.fileIDs = fileIDs;
        this.files = files;
        this.reducerInfo = reducerInfo;
        mapArray = new Map[26];
    }

    @Override
    public Boolean call() {
        if (fileIDs.size() != files.size()) {
            System.out.println("Number of IDs and files do not match!");
            return false;
        }
        try {
            for (int i = 0; i < fileIDs.size(); i++) {
                Map<String, Integer> wordCount = genWordCount(fileIDs.get(i), files.get(i));
            }
            return true;
        }
        catch (Exception e) {
            System.err.println("Error in mapper: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }

    private Map<String, Integer> genWordCount(int ID, File file) throws Exception {
        Map<String, Integer> wordCount = new HashMap<>();
        Scanner fileScanner = new Scanner(file);
        while(fileScanner.hasNext()){
            String[] line = fileScanner.nextLine().split(" ");
            for (String wordWithPunctuation: line) {
                String word = wordWithPunctuation.replaceAll("\\W", "");
                if (word.equals("")) continue;
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
        fileScanner.close();

        return wordCount;
    }

}
