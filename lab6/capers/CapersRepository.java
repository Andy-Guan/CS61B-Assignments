package capers;

import java.io.File;

import static capers.Dog.fromFile;
import static capers.Utils.*;

/** A repository for Capers 
 * @author Andy
 * The structure of a Capers Repository is as follows:
 *
 * .capers/ -- top level folder for all persistent data in your lab6 folder
 *    - dogs/ -- folder containing all of the persistent data for dogs
 *    - story -- file containing the current story
 *
 */
public class CapersRepository {
    /** Current Working Directory. */
    static final File CWD = new File(System.getProperty("user.dir"));

    /** Main metadata folder. */
    static final File CAPERS_FOLDER = Utils.join(CWD, ".capers");

    /**
     * Does required filesystem operations to allow for persistence.
     * (creates any necessary folders or files)
     */
    public static void setupPersistence() {
        if (!CAPERS_FOLDER.exists()) {
            CAPERS_FOLDER.mkdirs();
        }
    }

    /**
     * Appends the first non-command argument in args
     * to a file called `story` in the .capers directory.
     * @param text String of the text to be appended to the story
     */
    public static void writeStory(String text) {
        final File STORY_FILE = Utils.join(".capers", "story");
        String oldContent = "";
        if (STORY_FILE.exists()) {
            oldContent = Utils.readContentsAsString(STORY_FILE);
        }
        Utils.writeContents(STORY_FILE, oldContent + text);

    }

    /**
     * Creates and persistently saves a dog using the first
     * three non-command arguments of args (name, breed, age).
     * Also prints out the dog's information using toString().
     */
    public static void makeDog(String name, String breed, int age) {
        Dog newDog = new Dog(name, breed, age);
        newDog.saveDog();
        System.out.println(newDog.toString());
    }

    /**
     * Advances a dog's age persistently and prints out a celebratory message.
     * Also prints out the dog's information using toString().
     * Chooses dog to advance based on the first non-command argument of args.
     * @param name String name of the Dog whose birthday we're celebrating.
     */
    public static void celebrateBirthday(String name) {
        Dog dog = fromFile(name);
        System.out.println(dog.toString());
        dog.haveBirthday();
    }
}
