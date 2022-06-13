package org.vesta.db.impl;

import org.vesta.db.Dao;
import org.vesta.models.task.Task;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * I decided to implement the Task Dao as an in memory map as the tasks are given as input and then are not changed for
 * the duration TaskEngine runs. Therefore, it would be unnecessary to continuously request the tasks as they are not
 * changed.
 * <p>
 * In production, we would likely store the tasks in a distributed database and then cache them on the server. Instead
 * of implementing both the db and cache, I basically just implemented the cached in memory map here.
 */
public class TaskMapDaoImpl implements Dao<Task> {
    private final Map<String, Task> taskMap;
    private int nextAutoincrementId;

    public TaskMapDaoImpl() {
        taskMap = new HashMap<>();
        nextAutoincrementId = 0;
    }

    @Override
    public void insert(Task t) {
        String id = Integer.toString(nextAutoincrementId);
        nextAutoincrementId++;

        t.setId(id);
        taskMap.put(id, t);
    }

    @Override
    public Task updateField(String id, String field, Object value) {
        throw new UnsupportedOperationException("Task definitions are final and cannot be modified.");
    }

    @Override
    public Optional<Task> get(String id) {
        return Optional.of(taskMap.get(id));
    }

    @Override
    public List<Task> getByField(String field, String value) {
        return taskMap.values().stream().filter(task -> {
            Field declaredField;
            String taskValue;
            try {
                declaredField = task.getClass().getDeclaredField(field);
                declaredField.setAccessible(true);
                taskValue = declaredField.get(task).toString();
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(
                        String.format("Could not find or access field %s for task %s", field, task), e);
            }
            return taskValue.equals(value);
        }).collect(Collectors.toList());
    }

    @Override
    public List<Task> getAll() {
        return new ArrayList<>(taskMap.values());
    }
}
