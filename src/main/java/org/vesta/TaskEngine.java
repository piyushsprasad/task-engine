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
    private final List<Action> actions;

    public TaskEngine(String actionFilePath, String taskFilePath) {
        this(new HibernateConnection(), actionFilePath, taskFilePath);
    }

    public TaskEngine(HibernateConnection hibernateConnection, String actionFilePath, String taskFilePath) {
        this(hibernateConnection,
                new H2DaoImpl<Loan>(hibernateConnection, Loan.class),
                new H2DaoImpl<Borrower>(hibernateConnection, Borrower.class),
                new H2DaoImpl<TaskInstance>(hibernateConnection, TaskInstance.class),
                new TaskMapDaoImpl(),
                actionFilePath, taskFilePath);
    }

    public TaskEngine(HibernateConnection hibernateConnection,
                      Dao<Loan> loanDao, Dao<Borrower> borrowerDao, Dao<TaskInstance> taskInstanceDao,
                      Dao<Task> taskDao, String actionFilePath, String taskFilePath) {
        this.hibernateConnection = hibernateConnection;
        taskManager = new TaskManager(taskDao, taskInstanceDao);
        ActionFactory actionFactory = new ActionFactory(loanDao, borrowerDao, taskManager);
        JsonSerializer jsonSerializer = new JsonSerializer(actionFactory);

        // Parse actions and tasks from input files.
        actions = jsonSerializer.parseActions(actionFilePath);
        List<Task> tasks = jsonSerializer.parseTasks(taskFilePath);

        // Populate Task Map with inputted tasks.
        tasks.forEach(taskDao::insert);
    }

    public void process() {
        for (Action a : actions) {
            a.process();
            taskManager.printAllTasks();
        }
    }

    public void shutdown() {
        hibernateConnection.shutdown();
    }
}
