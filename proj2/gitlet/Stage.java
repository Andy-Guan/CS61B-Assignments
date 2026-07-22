package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class Stage implements Serializable {
    private HashMap<String, String> addition;
    private HashSet<String> removal;

    public Stage() {
        this.addition = new HashMap<>();
        this.removal = new HashSet<>();
    }
    public HashMap<String, String> getAddition() {
        return addition;
    }

    public HashSet<String> getRemoval() {
        return removal;
    }

}
