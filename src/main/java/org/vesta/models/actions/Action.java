package org.vesta.models.actions;

import lombok.Getter;
import lombok.ToString;
import org.vesta.db.TaskManager;
import org.vesta.models.task.EntityType;
import org.vesta.models.task.Task;
import org.vesta.models.task.TaskInstance;

import java.util.Map;

@Getter
@ToString
public abstract class Action {
    @ToString.Exclude
    private final TaskManager taskManager;
    public final ActionType type;
    private final String entityId;

    public Action(ActionType type, TaskManager taskManager, String entityId) {
        this.type = type;
        this.taskManager = taskManager;
        this.entityId = entityId;
    }

    /**
     * Overrided by action to actually process the update of the action.
     *
     * @return updated Entity
     */
    public abstract Object updateEntity();

    /**
     * Note: I debated here whether to return the updated tasks or leave it void like this. The upside to returning the
     * updated tasks is that I would not need to do another get() after the action processes to print all the tasks
     * (as I could cache the task instances and only change the updated ones). However, my thinking was that in
     * production we would likely not be printing all the tasks after each action, and so that functionality would not
     * be needed as much.
     *
     * @param updatedObj newly created or modified object.
     */
    public void updateTasks(Object updatedObj) {
        // Get already opened/associated task instances for this entity, keyed by their task definition ID.
        Map<String, TaskInstance> associatedTasks = taskManager.getRelatedTaskInstances(this.getEntityId());

        // Get all Task definitions for this entity type, keyed by their task definition ID.
        Map<String, Task> taskDefinitions = taskManager.getRelatedTaskDefinitions(this.getEntityType());

        // Update current tasks.
        for (TaskInstance taskInstance : associatedTasks.values()) {
            taskManager.updateTaskIfModified(taskInstance, updatedObj, taskDefinitions);
        }

        // Add new tasks
        for (Task taskDefinition : taskDefinitions.values()) {
            taskManager.insertTaskIfOpen(taskDefinition, this.getEntityId(), updatedObj, associatedTasks);
        }
    }

    public void process() {
        System.out.println("Processing " + this);
        Object object = updateEntity();
        updateTasks(object);
    }

    private EntityType getEntityType() {
        switch (this.type) {
            case CREATE_LOAN:
            case SET_LOAN_FIELD:
                return EntityType.LOAN;
            case CREATE_BORROWER:
            case SET_BORROWER_FIELD:
                return EntityType.BORROWER;
            default:
                throw new IllegalArgumentException("Invalid ActionType provided " + this.type);
        }
    }
}
