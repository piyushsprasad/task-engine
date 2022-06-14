package org.vesta;

import org.vesta.db.Dao;
import org.vesta.db.HibernateConnection;
import org.vesta.db.TaskManager;
import org.vesta.db.impl.H2DaoImpl;
import org.vesta.db.impl.TaskMapDaoImpl;
import org.vesta.json.JsonSerializer;
import org.vesta.models.Borrower;
import org.vesta.models.Loan;
import org.vesta.models.actions.Action;
import org.vesta.models.actions.ActionFactory;
import org.vesta.models.task.Task;
import org.vesta.models.task.TaskInstance;

import java.util.List;

public class TaskEngine {
    private final HibernateConnection hibernateConnection;
    private final TaskManager taskManager;
    private final JsonSerializer jsonSerializer;

    public TaskEngine(String taskFilePath) {
        this(new HibernateConnection(), taskFilePath);
    }

    public TaskEngine(HibernateConnection hibernateConnection, String taskFilePath) {
        this(hibernateConnection,
                new H2DaoImpl<Loan>(hibernateConnection, Loan.class),
                new H2DaoImpl<Borrower>(hibernateConnection, Borrower.class),
                new H2DaoImpl<TaskInstance>(hibernateConnection, TaskInstance.class),
                new TaskMapDaoImpl(),
                taskFilePath);
    }

    public TaskEngine(HibernateConnection hibernateConnection,
                      Dao<Loan> loanDao, Dao<Borrower> borrowerDao, Dao<TaskInstance> taskInstanceDao,
                      Dao<Task> taskDao, String taskFilePath) {
        this.hibernateConnection = hibernateConnection;
        this.taskManager = new TaskManager(taskDao, taskInstanceDao);

        ActionFactory actionFactory = new ActionFactory(loanDao, borrowerDao, taskManager);
        this.jsonSerializer = new JsonSerializer(actionFactory);

        // Parse tasks and save in memory
        addTasksFromFile(taskFilePath);
    }

    public void processActionsFromFile(String actionFilePath) {
        List<Action> actions = jsonSerializer.parseActions(actionFilePath);
        for (Action a : actions) {
            a.process();
            taskManager.printAllTasks();
        }
    }

    public void addTasksFromFile(String taskFilePath) {
        List<Task> tasks = jsonSerializer.parseTasks(taskFilePath);
        taskManager.insertAllTasks(tasks);
    }

    public void shutdown() {
        hibernateConnection.shutdown();
    }
}
