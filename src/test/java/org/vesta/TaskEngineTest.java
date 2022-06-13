package org.vesta;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.vesta.db.Dao;
import org.vesta.db.HibernateConnection;
import org.vesta.db.impl.H2DaoImpl;
import org.vesta.db.impl.TaskMapDaoImpl;
import org.vesta.models.Borrower;
import org.vesta.models.Loan;
import org.vesta.models.task.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class TaskEngineTest {
    private TaskEngine underTest;
    private HibernateConnection hibernateConnection;
    private Dao<Loan> loanDao;
    private Dao<Borrower> borrowerDao;
    private Dao<TaskInstance> taskInstanceDao;
    private Dao<Task> taskDao;

    @AfterEach
    public void shutdown() {
        underTest.shutdown();
    }

    @Test
    public void testTaskEngineWithSampleFiles() {
        String sampleActionsPath = "src/test/resources/sample_actions.json";
        String sampleTasksPath = "src/test/resources/sample_tasks.json";
        createUnderTestWithFiles(sampleActionsPath, sampleTasksPath);
        underTest.process();

        List<Loan> actualLoanList = loanDao.getAll();
        List<Loan> expectedLoanList =
                List.of(
                        Loan.builder()
                                .id("loan1")
                                .loanAmount(100000)
                                .loanType("Purchase")
                                .purchasePrice(500000)
                                .propertyAddress(null)
                                .build());
        assertThat(actualLoanList).usingRecursiveFieldByFieldElementComparator().containsExactlyElementsOf(expectedLoanList);

        List<Borrower> actualBorrowerList = borrowerDao.getAll();
        List<Borrower> expectedBorrowerList =
                List.of(
                        Borrower.builder()
                                .id("borr1")
                                .firstName("Jane")
                                .lastName("Smith")
                                .address(null)
                                .birthYear(null)
                                .loanId("loan1")
                                .build(),
                        Borrower.builder()
                                .id("borr2")
                                .firstName("Joseph")
                                .lastName("Smith")
                                .address("500 California St.")
                                .birthYear(null)
                                .loanId("loan1")
                                .build());
        assertThat(actualBorrowerList).usingRecursiveFieldByFieldElementComparator().containsExactlyElementsOf(expectedBorrowerList);

        List<Task> actualTaskList = taskDao.getAll();
        List<Task> expectedTaskList =
                List.of(
                        Task.builder()
                                .id("0")
                                .name("Require purchase price for purchase loans")
                                .entity(EntityType.LOAN)
                                .triggerConditions(new Condition[]{
                                        Condition.builder()
                                                .field(Loan.Fields.loanAmount)
                                                .comparator(Comparator.EXISTS)
                                                .value(null)
                                                .build(),
                                        Condition.builder()
                                                .field(Loan.Fields.loanType)
                                                .comparator(Comparator.EQUALS)
                                                .value("Purchase")
                                                .build()})
                                .completionConditions(new Condition[]{
                                        Condition.builder()
                                                .field(Loan.Fields.purchasePrice)
                                                .comparator(Comparator.EXISTS)
                                                .value(null)
                                                .build()})
                                .build(),
                        Task.builder()
                                .id("1")
                                .name("Require address for borrower")
                                .entity(EntityType.BORROWER)
                                .triggerConditions(new Condition[]{
                                        Condition.builder()
                                                .field(Borrower.Fields.firstName)
                                                .comparator(Comparator.EXISTS)
                                                .value(null)
                                                .build(),
                                        Condition.builder()
                                                .field(Borrower.Fields.lastName)
                                                .comparator(Comparator.EXISTS)
                                                .value(null)
                                                .build()})
                                .completionConditions(new Condition[]{
                                        Condition.builder()
                                                .field(Borrower.Fields.address)
                                                .comparator(Comparator.EXISTS)
                                                .value(null)
                                                .build()})
                                .build());
        assertThat(actualTaskList).usingRecursiveFieldByFieldElementComparator().containsExactlyElementsOf(expectedTaskList);

        List<TaskInstance> actualTaskInstanceList = taskInstanceDao.getAll();
        List<TaskInstance> expectedTaskInstanceList =
                List.of(
                        TaskInstance.builder()
                                .id("loan1:0")
                                .entityId("loan1")
                                .taskDefinitionId("0")
                                .name("Require purchase price for purchase loans")
                                .state(TaskState.COMPLETED)
                                .build(),
                        TaskInstance.builder()
                                .id("borr1:1")
                                .entityId("borr1")
                                .taskDefinitionId("1")
                                .name("Require address for borrower")
                                .state(TaskState.OPEN)
                                .build(),
                        TaskInstance.builder()
                                .id("borr2:1")
                                .entityId("borr2")
                                .taskDefinitionId("1")
                                .name("Require address for borrower")
                                .state(TaskState.COMPLETED)
                                .build());
        assertThat(actualTaskInstanceList).usingRecursiveFieldByFieldElementComparator().containsExactlyElementsOf(expectedTaskInstanceList);
    }

    private void createUnderTestWithFiles(String sampleActionsPath, String sampleTasksPath) {
        this.hibernateConnection = new HibernateConnection();
        this.loanDao = new H2DaoImpl<Loan>(hibernateConnection, Loan.class);
        this.borrowerDao = new H2DaoImpl<Borrower>(hibernateConnection, Borrower.class);
        this.taskInstanceDao = new H2DaoImpl<TaskInstance>(hibernateConnection, TaskInstance.class);
        this.taskDao = new TaskMapDaoImpl();
        underTest = new TaskEngine(hibernateConnection, loanDao, borrowerDao, taskInstanceDao, taskDao, sampleActionsPath, sampleTasksPath);
    }
}
