package org.vesta;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Required arguments not provided. Requires [actionFilePath] [taskFilePath]");
            return;
        }
        TaskEngine taskEngine = new TaskEngine(args[1]);
        taskEngine.processActionsFromFile(args[0]);
        taskEngine.shutdown();
    }
}