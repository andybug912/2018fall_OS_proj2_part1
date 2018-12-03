import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Callable;

public class MapperThread implements Callable<Boolean> {
    private List<Integer> fileIDs;
    private List<File> files;
    private List<String[]> reducerInfo;
    private Map<String, List<InvertedIndexItem>>[] invertedIndexArray;

    public MapperThread(List<Integer> fileIDs, List<File> files, List<String[]> reducerInfo) {
        this.fileIDs = fileIDs;
        this.files = files;
        this.reducerInfo = reducerInfo;
        invertedIndexArray = new Map[26];
    }

    @Override
    public Boolean call() {
        if (fileIDs.size() != files.size()) {
            System.out.println("Number of IDs and files do not match!");
            return false;
        }
        try {
            for (int i = 0; i < fileIDs.size(); i++) {
                Map<String, Integer> wordCount = genWordCount(files.get(i));
                genInvertedIndex(wordCount, fileIDs.get(i));
            }
            // send partial inverted index to reducer
            int startMapIndex = 0;
            List<ObjectInputStream> inputs = new ArrayList<>();
            for (String[] info: this.reducerInfo) {
                String serverIP = info[0];
                int serverPort = Integer.parseInt(info[1]);
                Socket socket = new Socket(serverIP, serverPort);
                final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                inputs.add(input);

                String validFirstLetterString = info[2];
                Set<Character> validFirstLetter = new HashSet<>();
                for (char c: validFirstLetterString.toCharArray()) {
                    validFirstLetter.add(c);
                }
                int endMapIndex = startMapIndex;
                Map<Character, Map<String, List<InvertedIndexItem>>> mapToSend = new HashMap<>();
                for (; endMapIndex < this.invertedIndexArray.length; endMapIndex++) {
                    if (!validFirstLetter.contains((char) ('a' + endMapIndex))) {
                        startMapIndex = endMapIndex;
                        break;
                    }
                    mapToSend.put((char) ('a' + endMapIndex), this.invertedIndexArray[endMapIndex]);
                }
                output.writeObject(Arrays.copyOfRange(this.invertedIndexArray, startMapIndex, endMapIndex));
            }

            for (ObjectInputStream input: inputs) {
                String response = (String) input.readObject();
                if (response.equals("FAIL")) {
                    return false;
                }
            }
            return true;
        }
        catch (Exception e) {
            System.err.println("Error in mapper: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }

    private Map<String, Integer> genWordCount(File file) throws Exception {
        Map<String, Integer> wordCount = new HashMap<>();
        Scanner fileScanner = new Scanner(file);
        while(fileScanner.hasNext()){
            String[] line = fileScanner.nextLine().split(" ");
            for (String wordWithPunctuation: line) {
                String word = wordWithPunctuation.replaceAll("\\W", "").toLowerCase();
                if (word.equals("") || word.charAt(0) < 'a' || word.charAt(0) > 'z') continue;
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
        fileScanner.close();

        return wordCount;
    }

    private void genInvertedIndex(Map<String, Integer> wordCount, int ID) {
        Iterator it = wordCount.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String word = (String) pair.getKey();
            int count = (int) pair.getValue();
            if (this.invertedIndexArray[word.charAt(0) - 'a'] == null) {
                this.invertedIndexArray[word.charAt(0) - 'a'] = new HashMap<>();
            }
            Map<String, List<InvertedIndexItem>> map = this.invertedIndexArray[word.charAt(0) - 'a'];
            if (!map.containsKey(word)) {
                List<InvertedIndexItem> list = new ArrayList<>();
                list.add(new InvertedIndexItem(ID, count));
                map.put(word, list);
            }
            else {
                boolean found = false;
                for (InvertedIndexItem item: map.get(word)) {
                    if (item.fileID == ID) {
                        found = true;
                        item.count += count;
                    }
                }
                if (!found) {
                    map.get(word).add(new InvertedIndexItem(ID, count));
                }
            }
        }
    }

//    private void sendPartialResultToReducer() throws Exception{
//        int startMapIndex = 0;
//        for (String[] info: this.reducerInfo) {
//            String serverIP = info[0];
//            int serverPort = Integer.parseInt(info[1]);
//            Socket socket = new Socket(serverIP, serverPort);
//            final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
//            final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
//
//            String validFirstLetterString = info[2];
//            Set<Character> validFirstLetter = new HashSet<>();
//            for (char c: validFirstLetterString.toCharArray()) {
//                validFirstLetter.add(c);
//            }
//            for (int endMapIndex = startMapIndex; endMapIndex < this.invertedIndexArray.length; endMapIndex++) {
//                if (!validFirstLetter.contains((char) ('a' + endMapIndex))) {
//                    startMapIndex = endMapIndex;
//                    endMapIndex--;
//                    break;
//                }
//            }
//            output
//        }
//    }
}
