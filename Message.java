import java.util.List;

public class Message {
    private String title;
    private String pathToBeIndexed;
    private List<String> keyWords;

    public Message(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getPathToBeIndexed() {
        return pathToBeIndexed;
    }

    public void setPathToBeIndexed(String pathToBeIndexed) {
        this.pathToBeIndexed = pathToBeIndexed;
    }

    public List<String> getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(List<String> keyWords) {
        this.keyWords = keyWords;
    }
}
