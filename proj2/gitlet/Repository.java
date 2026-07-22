package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *
 *.gitlet
 *    |--objects
 *    |--refs
 *        |--heads
 *    |--HEAD
 *    |--stageController
 *
 *  @author Andy
 */
public class Repository {


    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The directory for object */
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");
    /** The directory for branches */
    public static final File REFS_HEADS_DIR = join(GITLET_DIR, "refs", "heads");
    /** HEAD pointer */
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    /** Serialized Stage */
    public static final File STAGECONTROLLER = join(GITLET_DIR, "stageController");
    /** Remote Repository */
    public static final File REMOTES = Utils.join(GITLET_DIR, "remotes");
    /** Remote branches */
    public static final File REFS_REMOTES_DIR = Utils.join(GITLET_DIR, "refs", "remotes");

    /** Initialize the repository */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }

        GITLET_DIR.mkdir();
        OBJECT_DIR.mkdirs();
        REFS_HEADS_DIR.mkdirs();

        Commit initialCommit = Commit.createInitialCommit();
        byte[] commitBytes = serialize(initialCommit);
        String commitSha1 = Utils.sha1((Object) commitBytes);

        File commitFile = join(OBJECT_DIR, commitSha1);
        writeObject(commitFile, initialCommit);

        File masterFile = join(REFS_HEADS_DIR, "master");
        writeContents(masterFile, commitSha1);

        writeContents(HEAD, "refs/heads/master");

        Stage initialStage = new Stage();
        writeObject(STAGECONTROLLER, initialStage);
    }

    /** Get the current commit */
    public static Commit getCurrentCommit() {
        String headPath = Utils.readContentsAsString(HEAD);
        File branchFile = Utils.join(GITLET_DIR, headPath); //Joining the path of the pointer
        String commitSha1 = Utils.readContentsAsString(branchFile);
        File commitFile = Utils.join(OBJECT_DIR, commitSha1);

        return Utils.readObject(commitFile, Commit.class);
    }

    /** Add a file to the stage */
    public static void add(String filename) {
        File addFile = Utils.join(CWD, filename);
        if (!addFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        byte[] content = Utils.readContents(addFile);
        String fileSha1 = Utils.sha1(content);

        Commit currentCommit = getCurrentCommit();
        Stage currentStage = Utils.readObject(STAGECONTROLLER, Stage.class);
        String trackedSha1 = currentCommit.getTrackedFiles().get(filename);

        if (fileSha1.equals(trackedSha1)) {
            currentStage.getAddition().remove(filename);
            currentStage.getRemoval().remove(filename);
        } else {
            File blobFile = Utils.join(OBJECT_DIR, fileSha1);
            Utils.writeContents(blobFile, content);

            currentStage.getAddition().put(filename, fileSha1);
        }

        Utils.writeObject(STAGECONTROLLER, currentStage);
    }

    /** Commit with a message */
    public static void commit(String message) {
        if (!STAGECONTROLLER.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        Stage currentStage = Utils.readObject(STAGECONTROLLER, Stage.class);

        if (currentStage.getAddition().isEmpty() && currentStage.getRemoval().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        Commit parentCommit = getCurrentCommit();

        String headPath = Utils.readContentsAsString(HEAD);
        File branchFile = Utils.join(GITLET_DIR, headPath);
        String parentSha1 = Utils.readContentsAsString(branchFile);

        HashMap<String, String> newTrackedFiles = new HashMap<>(parentCommit.getTrackedFiles());

        newTrackedFiles.putAll(currentStage.getAddition());
        for (String fileToRemove : currentStage.getRemoval()) {
            newTrackedFiles.remove(fileToRemove);
        }

        List<String> parents = new java.util.ArrayList<>();
        parents.add(parentSha1);

        Commit newCommit = new Commit(message, new java.util.Date(), parents, newTrackedFiles);

        byte[] commitBytes = Utils.serialize(newCommit);
        String newCommitSha1 = Utils.sha1(commitBytes);
        File newCommitFile = Utils.join(OBJECT_DIR, newCommitSha1);
        Utils.writeObject(newCommitFile, newCommit);

        Utils.writeContents(branchFile, newCommitSha1);

        Stage emptyStage = new Stage();
        Utils.writeObject(STAGECONTROLLER, emptyStage);
    }

    /** Print the log */
    public static void log() {
        Commit currentCommit = getCurrentCommit();

        String headPath = Utils.readContentsAsString(HEAD);
        File branchFile = Utils.join(GITLET_DIR, headPath);
        String currentSha1 = Utils.readContentsAsString(branchFile);

        while (currentCommit != null) {
            System.out.println("===");
            System.out.println("commit " + currentSha1);
            List<String> parents = currentCommit.getParents();
            if (parents != null && parents.size() > 1) {
                String p1 = parents.get(0).substring(0, 7);
                String p2 = parents.get(1).substring(0, 7);
                System.out.println("Merge: " + p1 + " " + p2);
            }
            System.out.println("Date: " + Commit.DATEFORMAT.format(currentCommit.getTimestamp()));
            System.out.println(currentCommit.getMessage());
            System.out.println();

            if (parents == null || parents.isEmpty()) {
                break;
            } else {
                currentSha1 = parents.get(0); // the local branch
                File parentFile = Utils.join(OBJECT_DIR, currentSha1);
                currentCommit = Utils.readObject(parentFile, Commit.class);
            }
        }
    }

    public static void rm(String filename) {
        Stage currentStage = Utils.readObject(STAGECONTROLLER, Stage.class);
        Commit currentCommit = getCurrentCommit();

        boolean isTracked = currentCommit.getTrackedFiles().containsKey(filename);
        boolean isStaged = currentStage.getAddition().containsKey(filename);

        if (!isTracked && !isStaged) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        if (isStaged) {
            currentStage.getAddition().remove(filename);
        }

        if (isTracked) {
            currentStage.getRemoval().add(filename);
            File fileInCWD = Utils.join(CWD, filename);
            if (fileInCWD.exists()) {
                Utils.restrictedDelete(fileInCWD);
            }
        }
        Utils.writeObject(STAGECONTROLLER, currentStage);
    }

    /** checkout -- [filename] */
    public static void checkoutFile(String filename) {
        Commit currentCommit = getCurrentCommit();
        checkoutFileFromCommit(currentCommit, filename);
    }

    /** checkout [commit id] -- [filename] */
    public static void checkoutFile(String commitId, String filename) {
        commitId = getFullCommitId(commitId);

        File commitFile = Utils.join(OBJECT_DIR, commitId);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit targetCommit = Utils.readObject(commitFile, Commit.class);
        checkoutFileFromCommit(targetCommit, filename);
    }

    /** Helper for checkout */
    private static void checkoutFileFromCommit(Commit commit, String filename) {
        if (!commit.getTrackedFiles().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        String blobId = commit.getTrackedFiles().get(filename);
        File blobFile = Utils.join(OBJECT_DIR, blobId);

        byte[] content = Utils.readContents(blobFile);
        File targetFile = Utils.join(CWD, filename);
        Utils.writeContents(targetFile, content);
    }

    /** checkout to a specific branch */
    public static void checkoutBranch(String branchName) {
        File targetBranchFile = Utils.join(REFS_HEADS_DIR, branchName);

        if (!targetBranchFile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        String currentHead = Utils.readContentsAsString(HEAD); // "refs/heads/master"
        String currentBranchName = currentHead.replace("refs/heads/", "");

        if (branchName.equals(currentBranchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        Commit currentCommit = getCurrentCommit();
        String targetCommitSha1 = Utils.readContentsAsString(targetBranchFile);
        File targetCommitFile = Utils.join(OBJECT_DIR, targetCommitSha1);
        Commit targetCommit = Utils.readObject(targetCommitFile, Commit.class);

        checkoutCommit(targetCommit);

        Utils.writeContents(HEAD, "refs/heads/" + branchName);
    }

    /** Create a new branch */
    public static void branch(String branchName) {
        File newBranchFile = Utils.join(REFS_HEADS_DIR, branchName);

        if (newBranchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }

        String currentHead = Utils.readContentsAsString(HEAD);
        File currentBranchFile = Utils.join(GITLET_DIR, currentHead);
        String currentCommitSha1 = Utils.readContentsAsString(currentBranchFile);

        Utils.writeContents(newBranchFile, currentCommitSha1);
    }

    /** Remove a branch */
    public static void rmBranch(String branchName) {
        File targetBranchFile = Utils.join(REFS_HEADS_DIR, branchName);

        if (!targetBranchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        String currentHead = Utils.readContentsAsString(HEAD);
        String currentBranchName = currentHead.replace("refs/heads/", "");

        if (branchName.equals(currentBranchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        targetBranchFile.delete();
    }

    /** Return the git status */
    public static void status() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        Stage currentStage = Utils.readObject(STAGECONTROLLER, Stage.class);
        Commit currentCommit = getCurrentCommit();

        List<String> cwdFiles = Utils.plainFilenamesIn(CWD);
        if (cwdFiles == null) {
            cwdFiles = new java.util.ArrayList<>();
        }

        printBranches();
        printStagedFiles(currentStage);
        printRemovedFiles(currentStage);
        printModifiedNotStaged(currentCommit, currentStage, cwdFiles);
        printUntrackedFiles(currentCommit, currentStage, cwdFiles);
    }


    private static void printBranches() {
        System.out.println("=== Branches ===");
        String currentBranchName = Utils.readContentsAsString(HEAD).replace("refs/heads/", "");
        List<String> branches = Utils.plainFilenamesIn(REFS_HEADS_DIR);
        if (branches != null) {
            java.util.Collections.sort(branches);
            for (String branch : branches) {
                if (branch.equals(currentBranchName)) {
                    System.out.println("*" + branch);
                } else {
                    System.out.println(branch);
                }
            }
        }
        System.out.println();
    }

    private static void printStagedFiles(Stage stage) {
        System.out.println("=== Staged Files ===");
        List<String> stagedFiles = new java.util.ArrayList<>(stage.getAddition().keySet());
        java.util.Collections.sort(stagedFiles);
        for (String file : stagedFiles) {
            System.out.println(file);
        }
        System.out.println();
    }

    private static void printRemovedFiles(Stage stage) {
        System.out.println("=== Removed Files ===");
        List<String> removedFiles = new java.util.ArrayList<>(stage.getRemoval());
        java.util.Collections.sort(removedFiles);
        for (String file : removedFiles) {
            System.out.println(file);
        }
        System.out.println();
    }

    private static void printModifiedNotStaged(Commit commit, Stage stage, List<String> cwdFiles) {
        System.out.println("=== Modifications Not Staged For Commit ===");
        java.util.HashSet<String> modifiedNotStagedSet = new java.util.HashSet<>();
        HashMap<String, String> tracked = commit.getTrackedFiles();
        HashMap<String, String> added = stage.getAddition();
        HashSet<String> removed = stage.getRemoval();

        for (String file : cwdFiles) {
            File f = Utils.join(CWD, file);
            String cwdSha1 = Utils.sha1(Utils.readContents(f));

            if (tracked.containsKey(file)
                    && !cwdSha1.equals(tracked.get(file))
                    && !added.containsKey(file)
                    && !removed.contains(file)) {
                modifiedNotStagedSet.add(file + " (modified)");
            } else if (added.containsKey(file) && !cwdSha1.equals(added.get(file))) {
                modifiedNotStagedSet.add(file + " (modified)");
            }
        }

        for (String file : tracked.keySet()) {
            if (!cwdFiles.contains(file) && !removed.contains(file)) {
                modifiedNotStagedSet.add(file + " (deleted)");
            }
        }
        for (String file : added.keySet()) {
            if (!cwdFiles.contains(file)) {
                modifiedNotStagedSet.add(file + " (deleted)");
            }
        }

        List<String> modifiedNotStaged = new java.util.ArrayList<>(modifiedNotStagedSet);
        java.util.Collections.sort(modifiedNotStaged);
        for (String s : modifiedNotStaged) {
            System.out.println(s);
        }
        System.out.println();
    }

    private static void printUntrackedFiles(Commit commit, Stage stage, List<String> cwdFiles) {
        System.out.println("=== Untracked Files ===");
        List<String> untrackedFiles = new java.util.ArrayList<>();
        HashMap<String, String> tracked = commit.getTrackedFiles();
        HashMap<String, String> added = stage.getAddition();
        HashSet<String> removed = stage.getRemoval();

        for (String file : cwdFiles) {
            if (!added.containsKey(file)
                    && (!tracked.containsKey(file)
                    || removed.contains(file))) {
                untrackedFiles.add(file);
            }
        }
        java.util.Collections.sort(untrackedFiles);
        for (String file : untrackedFiles) {
            System.out.println(file);
        }
        System.out.println();
    }

    /** Find the split point */
    private static Commit getSplitPoint(String currentCommitSha1, String targetCommitSha1) {
        java.util.HashSet<String> currentAncestors = new java.util.HashSet<>();
        java.util.Queue<String> queue = new java.util.LinkedList<>();

        queue.add(currentCommitSha1);
        while (!queue.isEmpty()) {
            String sha1 = queue.poll();
            currentAncestors.add(sha1);

            File commitFile = Utils.join(OBJECT_DIR, sha1);
            Commit commit = Utils.readObject(commitFile, Commit.class);
            if (commit.getParents() != null) {
                queue.addAll(commit.getParents());
            }
        }

        // BFS
        java.util.Queue<String> targetQueue = new java.util.LinkedList<>();
        targetQueue.add(targetCommitSha1);

        while (!targetQueue.isEmpty()) {
            String sha1 = targetQueue.poll();

            if (currentAncestors.contains(sha1)) {
                File splitCommitFile = Utils.join(OBJECT_DIR, sha1);
                return Utils.readObject(splitCommitFile, Commit.class);
            }

            File commitFile = Utils.join(OBJECT_DIR, sha1);
            Commit commit = Utils.readObject(commitFile, Commit.class);
            if (commit.getParents() != null) {
                targetQueue.addAll(commit.getParents());
            }
        }

        return null; //init
    }

    public static void merge(String branchName) {
        Stage currentStage = Utils.readObject(STAGECONTROLLER, Stage.class);
        if (!currentStage.getAddition().isEmpty() || !currentStage.getRemoval().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        File targetBranchFile = Utils.join(REFS_HEADS_DIR, branchName);
        if (!targetBranchFile.exists()) {
            targetBranchFile = Utils.join(REFS_REMOTES_DIR, branchName);
        }
        if (!targetBranchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        String currentHead = Utils.readContentsAsString(HEAD);
        String currentBranchName = currentHead.replace("refs/heads/", "");
        if (branchName.equals(currentBranchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        String currentCommitSha1 = Utils.readContentsAsString(
                Utils.join(GITLET_DIR, currentHead));
        Commit currentCommit = Utils.readObject(
                Utils.join(OBJECT_DIR, currentCommitSha1), Commit.class);

        String targetCommitSha1 = Utils.readContentsAsString(targetBranchFile);
        Commit targetCommit = Utils.readObject(
                Utils.join(OBJECT_DIR, targetCommitSha1), Commit.class);


        Commit splitPoint = getSplitPoint(currentCommitSha1, targetCommitSha1);
        String splitSha1 = Utils.sha1(Utils.serialize(splitPoint));

        if (splitSha1.equals(targetCommitSha1)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }

        if (splitSha1.equals(currentCommitSha1)) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        HashMap<String, String> spFiles = splitPoint.getTrackedFiles();
        HashMap<String, String> cFiles = currentCommit.getTrackedFiles();
        HashMap<String, String> tFiles = targetCommit.getTrackedFiles();

        java.util.HashSet<String> allFiles = new java.util.HashSet<>();
        allFiles.addAll(spFiles.keySet());
        allFiles.addAll(cFiles.keySet());
        allFiles.addAll(tFiles.keySet());

        for (String file : allFiles) {
            String shaC = cFiles.get(file);
            boolean isUntracked = Utils.join(CWD, file).exists()
                    && (shaC == null || currentStage.getRemoval().contains(file))
                    && !currentStage.getAddition().containsKey(file);

            if (isUntracked) {
                String shaSP = spFiles.get(file);
                String shaT = tFiles.get(file);
                boolean willOverwrite = false;

                if (shaSP != null) {
                    if (shaSP.equals(shaC) && !shaSP.equals(shaT) && shaT != null) {
                        willOverwrite = true;
                    } else if (!java.util.Objects.equals(shaC, shaT)
                            && !shaSP.equals(shaC) && !shaSP.equals(shaT)) {
                        willOverwrite = true;
                    }
                } else {
                    if (shaC == null && shaT != null) {
                        willOverwrite = true;
                    } else if (shaC != null && shaT != null && !shaC.equals(shaT)) {
                        willOverwrite = true;
                    }
                }

                if (willOverwrite) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
        boolean hasConflict = processMergeFiles(allFiles, spFiles, cFiles, tFiles, targetCommit);

        String mergeMessage = "Merged " + branchName + " into " + currentBranchName + ".";


        if (hasConflict) {
            System.out.println("Encountered a merge conflict.");
        }

        finalizeMergeCommit(branchName, currentBranchName, currentCommitSha1,
                targetCommitSha1, currentCommit, currentStage);
    }

    /** Generate merge commit */
    private static void finalizeMergeCommit(String branchName, String currentBranchName,
                                            String p1, String p2,
                                            Commit currentCommit, Stage currentStage) {

        String mergeMessage = "Merged " + branchName + " into " + currentBranchName + ".";

        List<String> mergeParents = new java.util.ArrayList<>();
        mergeParents.add(p1);
        mergeParents.add(p2);

        currentStage = Utils.readObject(STAGECONTROLLER, Stage.class);
        HashMap<String, String> newTrackedFiles = new HashMap<>(currentCommit.getTrackedFiles());
        newTrackedFiles.putAll(currentStage.getAddition());
        for (String fileToRemove : currentStage.getRemoval()) {
            newTrackedFiles.remove(fileToRemove);
        }

        Commit mergeCommit = new Commit(mergeMessage, new java.util.Date(),
                mergeParents, newTrackedFiles);

        byte[] commitBytes = Utils.serialize(mergeCommit);
        String newCommitSha1 = Utils.sha1(commitBytes);
        File newCommitFile = Utils.join(OBJECT_DIR, newCommitSha1);
        Utils.writeObject(newCommitFile, mergeCommit);

        String headPath = Utils.readContentsAsString(HEAD);
        File branchFile = Utils.join(GITLET_DIR, headPath);
        Utils.writeContents(branchFile, newCommitSha1);

        Utils.writeObject(STAGECONTROLLER, new Stage());
    }

    /** Handle Merge files */
    private static boolean processMergeFiles(java.util.HashSet<String> allFiles,
                                             HashMap<String, String> spFiles,
                                             HashMap<String, String> cFiles,
                                             HashMap<String, String> tFiles,
                                             Commit targetCommit) {
        boolean hasConflict = false;

        for (String file : allFiles) {
            String shaSP = spFiles.get(file);
            String shaC = cFiles.get(file);
            String shaT = tFiles.get(file);

            if (shaSP != null) {
                if (shaSP.equals(shaC)
                        && !shaSP.equals(shaT)
                        && shaT != null) {
                    checkoutFileFromCommit(targetCommit, file);
                    stageAddition(file, shaT);
                } else if (shaSP.equals(shaC) && shaT == null) {
                    rm(file);
                } else if (!java.util.Objects.equals(shaC, shaT)
                        && !shaSP.equals(shaC)
                        && !shaSP.equals(shaT)) {
                    handleConflict(file, shaC, shaT);
                    hasConflict = true;
                }
            } else {
                if (shaC == null && shaT != null) {
                    checkoutFileFromCommit(targetCommit, file);
                    stageAddition(file, shaT);
                } else if (shaC != null && shaT != null && !shaC.equals(shaT)) {
                    handleConflict(file, shaC, shaT);
                    hasConflict = true;
                }
            }
        }
        return hasConflict;
    }

    /** handle the conflict for merge */
    private static void handleConflict(String file, String shaC, String shaT) {
        String contentC = "";
        if (shaC != null) {
            File blobC = Utils.join(OBJECT_DIR, shaC);
            contentC = Utils.readContentsAsString(blobC);
        }

        String contentT = "";
        if (shaT != null) {
            File blobT = Utils.join(OBJECT_DIR, shaT);
            contentT = Utils.readContentsAsString(blobT);
        }

        String conflictContent = "<<<<<<< HEAD\n"
                + contentC
                + "=======\n"
                + contentT
                + ">>>>>>>\n";

        File cwdFile = Utils.join(CWD, file);
        Utils.writeContents(cwdFile, conflictContent);

        byte[] bytes = conflictContent.getBytes();
        String newSha1 = Utils.sha1((Object) bytes);
        File blobFile = Utils.join(OBJECT_DIR, newSha1);
        Utils.writeContents(blobFile, bytes);

        Stage currentStage = Utils.readObject(STAGECONTROLLER, Stage.class);
        currentStage.getAddition().put(file, newSha1);
        Utils.writeObject(STAGECONTROLLER, currentStage);
    }

    /** Add the file temporarily */
    private static void stageAddition(String file, String sha1) {
        Stage currentStage = Utils.readObject(STAGECONTROLLER, Stage.class);
        currentStage.getAddition().put(file, sha1);
        Utils.writeObject(STAGECONTROLLER, currentStage);
    }

    /** Return the global log */
    public static void globalLog() {
        List<String> allObjectIds = Utils.plainFilenamesIn(OBJECT_DIR);
        if (allObjectIds == null) {
            return;
        }

        for (String objId : allObjectIds) {
            File objFile = Utils.join(OBJECT_DIR, objId);

            try {
                Commit commit = Utils.readObject(objFile, Commit.class);

                System.out.println("===");
                System.out.println("commit " + objId);

                List<String> parents = commit.getParents();
                if (parents != null && parents.size() > 1) {
                    String p1 = parents.get(0).substring(0, 7);
                    String p2 = parents.get(1).substring(0, 7);
                    System.out.println("Merge: " + p1 + " " + p2);
                }

                System.out.println("Date: " + Commit.DATEFORMAT.format(commit.getTimestamp()));
                System.out.println(commit.getMessage());
                System.out.println();

            } catch (IllegalArgumentException e) {
                continue;
            }
        }
    }

    /** Find the commit with the specific message */
    public static void find(String message) {
        List<String> allObjectIds = Utils.plainFilenamesIn(OBJECT_DIR);
        boolean found = false;

        if (allObjectIds != null) {
            for (String objId : allObjectIds) {
                File objFile = Utils.join(OBJECT_DIR, objId);
                try {
                    Commit commit = Utils.readObject(objFile, Commit.class);
                    if (commit.getMessage().equals(message)) {
                        System.out.println(objId);
                        found = true;
                    }
                } catch (IllegalArgumentException e) {
                    continue;
                }
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /** Reset to the commitID status */
    public static void reset(String commitId) {
        commitId = getFullCommitId(commitId);
        File targetCommitFile = Utils.join(OBJECT_DIR, commitId);
        if (!targetCommitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit targetCommit = Utils.readObject(targetCommitFile, Commit.class);
        Commit currentCommit = getCurrentCommit();

        checkoutCommit(targetCommit);

        String headPath = Utils.readContentsAsString(HEAD);
        File currentBranchFile = Utils.join(GITLET_DIR, headPath);
        Utils.writeContents(currentBranchFile, commitId);
    }

    /** Helper for checkout and reset */
    private static void checkoutCommit(Commit targetCommit) {
        Commit currentCommit = getCurrentCommit();

        for (String targetFileName : targetCommit.getTrackedFiles().keySet()) {
            File cwdFile = Utils.join(CWD, targetFileName);
            if (cwdFile.exists() && !currentCommit.getTrackedFiles().containsKey(targetFileName)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        for (String targetFileName : targetCommit.getTrackedFiles().keySet()) {
            String blobId = targetCommit.getTrackedFiles().get(targetFileName);
            File blobFile = Utils.join(OBJECT_DIR, blobId);
            byte[] content = Utils.readContents(blobFile);
            Utils.writeContents(Utils.join(CWD, targetFileName), content);
        }

        for (String currentFileName : currentCommit.getTrackedFiles().keySet()) {
            if (!targetCommit.getTrackedFiles().containsKey(currentFileName)) {
                File cwdFile = Utils.join(CWD, currentFileName);
                if (cwdFile.exists()) {
                    Utils.restrictedDelete(cwdFile);
                }
            }
        }

        Utils.writeObject(STAGECONTROLLER, new Stage());
    }

    /** Read the remote repository */
    private static java.util.HashMap<String, String> getRemotes() {
        if (!REMOTES.exists()) {
            return new java.util.HashMap<>();
        }
        return Utils.readObject(REMOTES, java.util.HashMap.class);
    }

    /** Add remote information */
    public static void addRemote(String remoteName, String remoteDir) {
        java.util.HashMap<String, String> remotes = getRemotes();

        if (remotes.containsKey(remoteName)) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }

        String normalizedDir = remoteDir.replace("/", java.io.File.separator);

        remotes.put(remoteName, normalizedDir);
        Utils.writeObject(REMOTES, remotes);
    }

    /** Remove a remote */
    public static void rmRemote(String remoteName) {
        java.util.HashMap<String, String> remotes = getRemotes();

        if (!remotes.containsKey(remoteName)) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }

        remotes.remove(remoteName);
        Utils.writeObject(REMOTES, remotes);
    }

    /** Fetch from the remote */
    public static void fetch(String remoteName, String remoteBranchName) {
        java.util.HashMap<String, String> remotes = getRemotes();

        if (!remotes.containsKey(remoteName)) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        File remoteGitletDir = new File(remotes.get(remoteName));
        if (!remoteGitletDir.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        File remoteBranchFile = Utils.join(remoteGitletDir, "refs", "heads", remoteBranchName);
        if (!remoteBranchFile.exists()) {
            System.out.println("That remote does not have that branch.");
            System.exit(0);
        }

        String remoteHeadSha1 = Utils.readContentsAsString(remoteBranchFile);
        File remoteObjectsDir = Utils.join(remoteGitletDir, "objects");

        fetchObjects(remoteHeadSha1, remoteObjectsDir);

        File localRemoteDir = Utils.join(REFS_REMOTES_DIR, remoteName);
        localRemoteDir.mkdirs();
        File localRemoteBranch = Utils.join(localRemoteDir, remoteBranchName);
        Utils.writeContents(localRemoteBranch, remoteHeadSha1);
    }

    /** Helper for fetch */
    private static void fetchObjects(String commitSha1, File remoteObjectsDir) {
        File localCommitFile = Utils.join(OBJECT_DIR, commitSha1);
        if (localCommitFile.exists()) {
            return;
        }

        File remoteCommitFile = Utils.join(remoteObjectsDir, commitSha1);
        byte[] commitData = Utils.readContents(remoteCommitFile);
        Utils.writeContents(localCommitFile, commitData);

        Commit commit = Utils.readObject(localCommitFile, Commit.class);

        for (String blobSha1 : commit.getTrackedFiles().values()) {
            File localBlobFile = Utils.join(OBJECT_DIR, blobSha1);
            if (!localBlobFile.exists()) {
                File remoteBlobFile = Utils.join(remoteObjectsDir, blobSha1);
                byte[] blobData = Utils.readContents(remoteBlobFile);
                Utils.writeContents(localBlobFile, blobData);
            }
        }

        List<String> parents = commit.getParents();
        if (parents != null) {
            for (String parentSha1 : parents) {
                fetchObjects(parentSha1, remoteObjectsDir);
            }
        }
    }

    /** Pull from remote */
    public static void pull(String remoteName, String remoteBranchName) {
        fetch(remoteName, remoteBranchName);
        String targetBranch = remoteName + "/" + remoteBranchName;
        merge(targetBranch);
    }

    /** Push to the remote */
    public static void push(String remoteName, String remoteBranchName) {
        java.util.HashMap<String, String> remotes = getRemotes();

        if (!remotes.containsKey(remoteName)) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        File remoteGitletDir = new File(remotes.get(remoteName));
        if (!remoteGitletDir.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        File remoteBranchFile = Utils.join(remoteGitletDir, "refs", "heads", remoteBranchName);
        String remoteHeadSha1 = "";
        if (remoteBranchFile.exists()) {
            remoteHeadSha1 = Utils.readContentsAsString(remoteBranchFile);
        }

        Commit currentCommit = getCurrentCommit();
        String localHeadSha1 = Utils.sha1(Utils.serialize(currentCommit));

        if (!remoteHeadSha1.isEmpty() && !isHistoryContains(localHeadSha1, remoteHeadSha1)) {
            System.out.println("Please pull down to catch up with remote.");
            System.exit(0);
        }

        File remoteObjectsDir = Utils.join(remoteGitletDir, "objects");
        pushObjects(localHeadSha1, remoteHeadSha1, remoteObjectsDir);

        Utils.writeContents(remoteBranchFile, localHeadSha1);
    }

    /** Check history */
    private static boolean isHistoryContains(String currentSha1, String targetSha1) {
        if (currentSha1.equals(targetSha1)) {
            return true;
        }

        File commitFile = Utils.join(OBJECT_DIR, currentSha1);
        if (!commitFile.exists()) {
            return false;
        }

        Commit commit = Utils.readObject(commitFile, Commit.class);
        List<String> parents = commit.getParents();

        if (parents == null || parents.isEmpty()) {
            return false;
        }

        for (String parent : parents) {
            if (isHistoryContains(parent, targetSha1)) {
                return true;
            }
        }
        return false;
    }

    /** Copy the local object */
    private static void pushObjects(String currentSha1, String stopSha1, File remoteObjectsDir) {
        if (currentSha1.equals(stopSha1)) {
            return;
        }

        File localCommitFile = Utils.join(OBJECT_DIR, currentSha1);
        Commit commit = Utils.readObject(localCommitFile, Commit.class);

        File remoteCommitFile = Utils.join(remoteObjectsDir, currentSha1);
        Utils.writeContents(remoteCommitFile, Utils.readContents(localCommitFile));

        for (String blobSha1 : commit.getTrackedFiles().values()) {
            File localBlobFile = Utils.join(OBJECT_DIR, blobSha1);
            File remoteBlobFile = Utils.join(remoteObjectsDir, blobSha1);
            if (localBlobFile.exists() && !remoteBlobFile.exists()) {
                Utils.writeContents(remoteBlobFile, Utils.readContents(localBlobFile));
            }
        }

        List<String> parents = commit.getParents();
        if (parents != null) {
            for (String parent : parents) {
                pushObjects(parent, stopSha1, remoteObjectsDir);
            }
        }
    }

    /**
     * Helper: Convert a short UID to a full 40-character SHA-1 UID.
     * If the input is already 40 chars, or no match is found, it returns the original input.
     */
    private static String getFullCommitId(String shortId) {
        if (shortId.length() == 40) {
            return shortId;
        }

        List<String> allObjectIds = Utils.plainFilenamesIn(OBJECT_DIR);
        if (allObjectIds != null) {
            for (String objId : allObjectIds) {
                if (objId.startsWith(shortId)) {
                    return objId;
                }
            }
        }
        return shortId;
    }
}
