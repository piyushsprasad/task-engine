package org.vesta.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.vesta.db.Dao;
import org.vesta.models.Loan;
import org.vesta.models.task.*;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@RunWith(MockitoJUnitRunner.class)
public class TaskManagerTest {
    @Mock
    private Dao<Task> mockTaskDao;
    @Mock
    private Dao<TaskInstance> mockTaskInstanceDao;

    private TaskManager underTest;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        underTest = new TaskManager(mockTaskDao, mockTaskInstanceDao);
    }

    @Test
    public void testUpdateTaskIfModifiedCompleted() {
        String entityId = "entityId";
        String tdId1 = "0";
        TaskInstance taskInstance =
                TaskInstance.builder()
                        .id(entityId + ":" + tdId1)
                        .entityId(entityId)
                        .taskDefinitionId(tdId1)
                        .name("task 1")
                        .state(TaskState.COMPLETED)
                        .build();
        underTest.updateTaskIfModified(taskInstance, new Object(), new HashMap<>());
        verifyNoInteractions(mockTaskInstanceDao);
    }

    @Test
    public void testUpdateTaskIfModifiedNotModified() {
        String entityId = "entityId";
        String tdId1 = "0";
        TaskInstance taskInstance =
                TaskInstance.builder()
                        .id(entityId + ":" + tdId1)
                        .entityId(entityId)
                        .taskDefinitionId(tdId1)
                        .name("task 1")
                        .state(TaskState.OPEN)
                        .build();
        Task taskDefinition =
                Task.builder()
                        .name("task 1")
                        .entity(EntityType.LOAN)
                        .id(tdId1)
                        .triggerConditions(new Condition[]{
                                Condition.builder()
                                        .field(Loan.Fields.loanAmount)
                                        .comparator(Comparator.EXISTS)
                                        .build()
                        })
                        .build();
        Map<String, Task> taskDefinitions = new HashMap<>();
        taskDefinitions.put(tdId1, taskDefinition);
        Loan loan = new Loan("abc");
        loan.setLoanAmount(100);
        underTest.updateTaskIfModified(taskInstance, loan, taskDefinitions);
        verifyNoInteractions(mockTaskInstanceDao);
    }

    @Test
    public void testUpdateTaskIfModifiedShouldUpdate() {
        String entityId = "entityId";
        String tdId1 = "0";
        String taskInstanceId = entityId + ":" + tdId1;
        TaskInstance taskInstance =
                TaskInstance.builder()
                        .id(taskInstanceId)
                        .entityId(entityId)
                        .taskDefinitionId(tdId1)
                        .name("task 1")
                        .state(TaskState.CANCELLED)
                        .build();
        Task taskDefinition =
                Task.builder()
                        .name("task 1")
                        .entity(EntityType.LOAN)
                        .id(tdId1)
                        .triggerConditions(new Condition[]{
                                Condition.builder()
                                        .field(Loan.Fields.loanAmount)
                                        .comparator(Comparator.EXISTS)
                                        .build()
                        })
                        .build();
        Map<String, Task> taskDefinitions = new HashMap<>();
        taskDefinitions.put(tdId1, taskDefinition);
        Loan loan = new Loan("abc");
        loan.setLoanAmount(100);
        underTest.updateTaskIfModified(taskInstance, loan, taskDefinitions);
        verify(mockTaskInstanceDao).updateField(taskInstanceId, TaskInstance.Fields.state, TaskState.OPEN);
    }

    @Test
    public void testInsertTaskIfOpenAlreadyExists() {
        String entityId = "entityId";
        String tdId1 = "0";
        String taskInstanceId = entityId + ":" + tdId1;
        TaskInstance taskInstance =
                TaskInstance.builder()
                        .id(taskInstanceId)
                        .entityId(entityId)
                        .taskDefinitionId(tdId1)
                        .name("task 1")
                        .state(TaskState.CANCELLED)
                        .build();
        Map<String, TaskInstance> taskInstanceMap = new HashMap<>();
        taskInstanceMap.put(tdId1, taskInstance);
        Task taskDefinition =
                Task.builder()
                        .name("task 1")
                        .entity(EntityType.LOAN)
                        .id(tdId1)
                        .triggerConditions(new Condition[]{
                                Condition.builder()
                                        .field(Loan.Fields.loanAmount)
                                        .comparator(Comparator.EXISTS)
                                        .build()
                        })
                        .build();
        underTest.insertTaskIfOpen(taskDefinition, entityId, new Object(), taskInstanceMap);
        Mockito.verifyNoInteractions(mockTaskInstanceDao);
    }

    @Test
    public void testInsertTaskIfOpenNotOpenYet() {
        String entityId = "entityId";
        String tdId1 = "0";
        String taskInstanceId = entityId + ":" + tdId1;
        Task taskDefinition =
                Task.builder()
                        .name("task 1")
                        .entity(EntityType.LOAN)
                        .id(tdId1)
                        .triggerConditions(new Condition[]{
                                Condition.builder()
                                        .field(Loan.Fields.loanAmount)
                                        .comparator(Comparator.EXISTS)
                                        .build()
                        })
                        .build();
        Loan loan = new Loan("abc");
        underTest.insertTaskIfOpen(taskDefinition, entityId, loan, new HashMap<>());
        Mockito.verifyNoInteractions(mockTaskInstanceDao);
    }

    @Test
    public void testInsertTaskIfOpenDoesInsert() {
        String entityId = "entityId";
        String tdId1 = "0";
        String taskInstanceId = entityId + ":" + tdId1;
        Task taskDefinition =
                Task.builder()
                        .name("task 1")
                        .entity(EntityType.LOAN)
                        .id(tdId1)
                        .triggerConditions(new Condition[]{
                                Condition.builder()
                                        .field(Loan.Fields.loanAmount)
                                        .comparator(Comparator.EXISTS)
                                        .build()
                        })
                        .build();
        Loan loan = new Loan("abc");
        loan.setLoanAmount(100);
        underTest.insertTaskIfOpen(taskDefinition, entityId, loan, new HashMap<>());

        TaskInstance expectedInsertedInstance =
                new TaskInstance(entityId, tdId1, taskDefinition.getName(), TaskState.OPEN);
        Mockito.verify(mockTaskInstanceDao).insert(ArgumentMatchers.refEq(expectedInsertedInstance));
    }

}

