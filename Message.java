import java.io.Serializable;
import java.util.List;
import java.util.PriorityQueue;

public class Message implements Serializable {
    private String title;
    private String pathToBeIndexed;
    private List<String> keyWords;
    private String message;
    private List<String> queryResult;

    public Message(String title) {
        this.title = title;
    }

    public Message(String title,String pathToBeIndexed){
        this.title = title;
        this.pathToBeIndexed = pathToBeIndexed;
    }

    public Message(String title, List<String> keyWords){
        this.title = title;
        this.keyWords = keyWords;
    }

    public Message(String title, List<String> queryResult, boolean isQuery){
        this.title = title;
        this.queryResult = queryResult;
    }

    public String getTitle() {
        return title;
    }

    public String getPathToBeIndexed() {
        return pathToBeIndexed;
    }

    public List<String> getKeyWords() {
        return keyWords;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getQueryResult() {
        return queryResult;
    }
}
