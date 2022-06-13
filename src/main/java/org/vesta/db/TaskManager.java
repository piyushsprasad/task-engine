package org.vesta.db;

import org.vesta.models.task.EntityType;
import org.vesta.models.task.Task;
import org.vesta.models.task.TaskInstance;
import org.vesta.models.task.TaskState;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TaskManager {
    private final Dao<Task> taskDao;
    private final Dao<TaskInstance> taskInstanceDao;

    public TaskManager(Dao<Task> taskDao, Dao<TaskInstance> taskInstanceDao) {
        this.taskDao = taskDao;
        this.taskInstanceDao = taskInstanceDao;
    }

    // Get task instances for the provided entityId.
    public Map<String, TaskInstance> getRelatedTaskInstances(String entityId) {
        return taskInstanceDao
                .getByField(TaskInstance.Fields.entityId, entityId)
                .stream()
                .collect(Collectors.toMap(TaskInstance::getTaskDefinitionId, Function.identity()));
    }

    // Get all task definitions for the entity type.
    public Map<String, Task> getRelatedTaskDefinitions(EntityType entityType) {
        return taskDao
                .getByField(Task.Fields.entity, entityType.getTypeName())
                .stream()
                .collect(Collectors.toMap(Task::getId, Function.identity()));
    }

    // Only update the task if the state has changed.
    public void updateTaskIfModified(
            TaskInstance currentTask, Object updatedObj, Map<String, Task> taskDefinitions) {
        // Do not modify already completed tasks.
        TaskState currentState = currentTask.getState();
        if (currentState == TaskState.COMPLETED) {
            return;
        }
        Task taskDefinition = taskDefinitions.get(currentTask.getTaskDefinitionId());
        TaskState updatedState = taskDefinition.computeState(updatedObj);

        if (updatedState != currentState) {
            taskInstanceDao.updateField(currentTask.getId(), TaskInstance.Fields.state, updatedState);
        }
    }

    // Insert a new task instance if a new one has opened.
    public void insertTaskIfOpen(
            Task taskDefinition, String entityId, Object updatedObj, Map<String, TaskInstance> currentTasks) {
        if (currentTasks.containsKey(taskDefinition.getId())) {
            // Already have an instance of the task associated to this entity.
            return;
        }
        TaskState taskState = taskDefinition.computeState(updatedObj);
        if (taskState == TaskState.CANCELLED) {
            // Task triggers not met yet, so do not open task.
            return;
        }

        TaskInstance taskInstance =
                new TaskInstance(
                        entityId, taskDefinition.getId(), taskDefinition.getName(), taskState);
        taskInstanceDao.insert(taskInstance);
    }

    public void printAllTasks() {
        List<TaskInstance> allTaskInstances = taskInstanceDao.getAll();
        if (allTaskInstances.isEmpty()) {
            System.out.println("No tasks are open currently ------------------");
            System.out.println();
            return;
        }
        allTaskInstances.sort(Comparator.comparing(TaskInstance::getId));
        System.out.println("Printing all opened tasks -------------------- ");
        allTaskInstances.forEach(System.out::println);
        System.out.println("Finished printing all opened tasks -----------");
        System.out.println();
    }
}
