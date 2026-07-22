package gitlet;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/** Represents a gitlet commit object.
 *
 *  @author Andy
 */
public class Commit implements Serializable {

    /** The message of this Commit. */
    private String message;

    /** The exact time in Date format */
    private Date timestamp;

    /** Track the parent */
    private List<String> parents;

    /** Track the files */
    private HashMap<String, String> trackedFiles;

    /** Standard timestamp */
    static final SimpleDateFormat DATEFORMAT =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);

    public Commit(String message,
                  Date timestamp,
                  List<String> parents,
                  HashMap<String, String> trackedFiles) {
        this.message = message;
        this.timestamp = timestamp;
        this.parents = parents;
        this.trackedFiles = trackedFiles;
    }

    public static Commit createInitialCommit() {
        return new Commit(
                "initial commit",
                new Date(0), // 1970-01-01 00:00:00
                new java.util.ArrayList<>(),
                new HashMap<>()
        );
    }

    public HashMap<String, String> getTrackedFiles() {
        return trackedFiles;
    }

    public java.util.List<String> getParents() {
        return parents;
    }

    public String getMessage() {
        return message;
    }

    public java.util.Date getTimestamp() {
        return timestamp;
    }
}

