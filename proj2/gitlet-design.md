# Gitlet Design Document

**Name**: Andy

## Classes and Data Structures

### 1. Repository
This is the main orchestrator class of the Gitlet system. It contains all the core logic and handles file I/O operations, ensuring the `.gitlet` directory is correctly updated after every command.

#### Fields
*   `public static final File CWD`: The Current Working Directory.
*   `public static final File GITLET_DIR`: The `.gitlet` hidden directory.
*   `public static final File OBJECT_DIR`: Directory storing all serialized Commits and Blobs.
*   `public static final File REFS_HEADS_DIR`: Directory for storing local branch pointers.
*   `public static final File HEAD`: File keeping track of the current active branch.
*   `public static final File STAGECONTROLLER`: File storing the serialized `Stage` object.
*   `public static final File REMOTES`: File storing serialized configurations for Remote Repositories.
*   `public static final File REFS_REMOTES_DIR`: Directory storing branch pointers fetched from remotes.

### 2. Commit
Represents a snapshot of the project at a specific point in time. It implements `Serializable` so it can be stored in the `objects` directory.

#### Fields
*   `private String message`: The commit message.
*   `private Date timestamp`: The time when the commit was created (Epoch time for initial commit).
*   `private List<String> parents`: A list of parent commit SHA-1 IDs (size 1 for normal, size 2 for merges).
*   `private HashMap<String, String> trackedFiles`: A mapping of filenames to their corresponding Blob SHA-1 IDs.

### 3. Stage
Represents the Staging Area. It keeps track of files that are ready to be added or removed in the next commit. It is serialized into the `STAGECONTROLLER` file.

#### Fields
*   `private HashMap<String, String> addition`: Maps filenames to Blob SHA-1s for files staged for addition.
*   `private HashSet<String> removal`: A set of filenames staged for removal.

---

## Algorithms

### 1. add(String filename)
1. Checks if the file exists in `CWD`.
2. Computes the SHA-1 of the file's contents.
3. Retrieves the current `Commit` and checks if the file is already tracked with the exact same content.
4. If unchanged, removes it from the `Stage` (if it was there).
5. If changed, saves the file contents as a Blob in `OBJECT_DIR`, adds it to `Stage.addition`, and serializes the `Stage`.

### 2. commit(String message)
1. Deserializes the `Stage`. Aborts if both `addition` and `removal` are empty.
2. Clones the `trackedFiles` mapping from the parent `Commit`.
3. Updates the mapping with entries from `Stage.addition` and removes entries found in `Stage.removal`.
4. Creates a new `Commit` object, computes its SHA-1, and saves it to `OBJECT_DIR`.
5. Updates the current branch file to point to this new SHA-1 and clears the `Stage`.

### 3. merge(String branchName)
1. Finds the Split Point (Lowest Common Ancestor) using Breadth-First Search (BFS) on the commit DAG.
2. Compares the Split Point, Current Commit, and Target Commit to apply the 8 merge rules.
3. Handles conflicts by writing a specific formatted string into the file and staging it.
4. Automatically generates a new Merge Commit with two parents.

### 4. Remote Operations (fetch, pull, push)
1.  **fetch**: Validates the remote directory and branch. Recursively copies missing `Commit` and Blob objects from the remote's `OBJECT_DIR` to the local `OBJECT_DIR`. Creates or updates the remote branch pointer inside the local `REFS_REMOTES_DIR`.
2.  **pull**: First executes `fetch` for the specified remote branch. Then executes `merge` by targeting the newly fetched remote branch pointer.
3.  **push**: Checks if the remote branch's Head SHA-1 exists in the local branch's history to ensure the local repository is up to date. Recursively copies missing objects from the local `OBJECT_DIR` to the remote `OBJECT_DIR`. Updates the remote branch file to point to the local Head SHA-1.

---

## Persistence

The Gitlet system relies heavily on the file system for state management. Memory is completely cleared after each CLI command execution.

### Directory Structure Strategy
*   **`.gitlet/objects/`**: Uses content-addressable storage. Every `Commit` and file content (Blob) is serialized and saved here. The filename is the 40-character SHA-1 hash of its contents.
*   **`.gitlet/refs/heads/`**: Contains text files named after branches (e.g., `master`). Each file contains exactly one string: the SHA-1 of the latest Commit on that local branch.
*   **`.gitlet/HEAD`**: Contains the relative path to the current branch file (e.g., `refs/heads/master`), acting as an indirect pointer.
*   **`.gitlet/stageController`**: Stores the single serialized `Stage` object.
*   **`.gitlet/remotes`**: Stores a serialized `HashMap<String, String>` that maps remote repository names to their normalized file directory paths.
*   **`.gitlet/refs/remotes/`**: Similar to `heads`, this directory stores text files representing fetched remote branch pointers (e.g., `R1/master`).

### State Saving/Loading Workflow
1. At the start of any command (except `init`), the system reads pointers (`HEAD` -> `branch file`) to locate the current `Commit` SHA-1.
2. It uses `Utils.readObject()` to deserialize the `Commit` and the `Stage` from the disk into memory.
3. After applying the command's logic, any modified objects (new Blobs, updated Stage, new Commits, updated pointers) are written back to the disk using `Utils.writeObject()` or `Utils.writeContents()`.