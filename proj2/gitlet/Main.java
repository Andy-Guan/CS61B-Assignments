package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Andy
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs(args, 1);
                Repository.init();
                break;
            case "add":
                validateNumArgs(args, 2);
                String filename = args[1];
                Repository.add(filename);
                break;
            case "commit":
                if (args.length == 1) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                validateNumArgs(args, 2);

                String message = args[1];
                if (message.trim().isEmpty()) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                Repository.commit(message);
                break;
            case "log":
                validateNumArgs(args, 1);
                Repository.log();
                break;
            case "rm":
                validateNumArgs(args, 2);
                String rmFile = args[1];
                Repository.rm(rmFile);
                break;
            case "checkout":
                if (args.length == 2) {
                    String branchName = args[1];
                    Repository.checkoutBranch(branchName);
                }
                else if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    filename = args[2];
                    Repository.checkoutFile(filename);
                }
                else if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    String commitId = args[1];
                    filename = args[3];
                    Repository.checkoutFile(commitId, filename);
                }
                else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;
            case "branch":
                validateNumArgs(args, 2);
                String newBranchName = args[1];
                Repository.branch(newBranchName);
                break;
            case "rm-branch":
                validateNumArgs(args, 2);
                String branchToRemove = args[1];
                Repository.rmBranch(branchToRemove);
                break;
            case "status":
                validateNumArgs(args, 1);
                Repository.status();
                break;
            case "global-log":
                validateNumArgs(args, 1);
                Repository.globalLog();
                break;
            case "find":
                validateNumArgs(args, 2);
                String findMessage = args[1];
                Repository.find(findMessage);
                break;
            case "reset":
                validateNumArgs(args, 2);
                String resetCommitId = args[1];
                Repository.reset(resetCommitId);
                break;
            case "add-remote":
                if (args.length != 3) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.addRemote(args[1], args[2]);
                break;

            case "rm-remote":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.rmRemote(args[1]);
                break;

            case "fetch":
                if (args.length != 3) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.fetch(args[1], args[2]);
                break;

            case "push":
                if (args.length != 3) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.push(args[1], args[2]);
                break;

            case "pull":
                if (args.length != 3) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.pull(args[1], args[2]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }



    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
