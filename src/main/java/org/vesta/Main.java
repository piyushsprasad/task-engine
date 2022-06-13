package org.vesta;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Required arguments not provided. Requires [actionFilePath] [taskFilePath]");
            return;
        }
//        String sampleActionsPath = "src/test/resources/sample_actions.json";
//        String sampleTasksPath = "src/test/resources/sample_tasks.json";
        TaskEngine taskEngine = new TaskEngine(args[0], args[1]);
        taskEngine.process();
        taskEngine.shutdown();
    }
}