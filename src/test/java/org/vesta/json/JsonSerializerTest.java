package org.vesta.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.vesta.db.Dao;
import org.vesta.models.Borrower;
import org.vesta.models.Loan;
import org.vesta.models.actions.Action;
import org.vesta.models.actions.ActionFactory;
import org.vesta.models.task.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSerializerTest {
    private ActionFactory actionFactory;
    private JsonSerializer underTest;
    private String sampleActionsPath = "src/test/resources/sample_actions.json";
    private String sampleTasksPath = "src/test/resources/sample_tasks.json";

    @BeforeEach
    public void setup() {
        Dao<Loan> mockLoanDao = Mockito.mock(Dao.class);
        Dao<Borrower> mockBorrowerDao = Mockito.mock(Dao.class);
        Dao<Task> mockTaskDao = Mockito.mock(Dao.class);
        Dao<TaskInstance> mockTaskInstanceDao = Mockito.mock(Dao.class);
        TaskManager taskManager = new TaskManager(mockTaskDao, mockTaskInstanceDao);
        actionFactory = new ActionFactory(mockLoanDao, mockBorrowerDao, taskManager);
        underTest = new JsonSerializer(actionFactory);
    }

    @Test
    public void testParseActions() {
        List<Action> actualActions = underTest.parseActions(sampleActionsPath);

        String loanId1 = "loan1";
        String borrowerId1 = "borr1";
        String borrowerId2 = "borr2";
        List<Action> expectedActions = new ArrayList<>();
        expectedActions.add(actionFactory.createLoanAction(loanId1));
        expectedActions.add(actionFactory.createBorrowerAction(borrowerId1, loanId1));
        expectedActions.add(actionFactory.createBorrowerAction(borrowerId2, loanId1));
        expectedActions.add(actionFactory.setLoanFieldAction(loanId1, Loan.Fields.loanAmount, 100000));
        expectedActions.add(actionFactory.setLoanFieldAction(loanId1, Loan.Fields.loanType, "Purchase"));
        expectedActions.add(actionFactory.setBorrowerFieldAction(borrowerId1, Borrower.Fields.firstName, "Jane"));
        expectedActions.add(actionFactory.setBorrowerFieldAction(borrowerId1, Borrower.Fields.lastName, "Smith"));
        expectedActions.add(actionFactory.setBorrowerFieldAction(borrowerId2, Borrower.Fields.firstName, "John"));
        expectedActions.add(actionFactory.setBorrowerFieldAction(borrowerId2, Borrower.Fields.lastName, "Smith"));
        expectedActions.add(actionFactory.setBorrowerFieldAction(borrowerId2, Borrower.Fields.firstName, null));
        expectedActions.add(actionFactory.setLoanFieldAction(loanId1, Loan.Fields.purchasePrice, 500000));
        expectedActions.add(actionFactory.setBorrowerFieldAction(borrowerId2, Borrower.Fields.firstName, "Joseph"));
        expectedActions.add(actionFactory.setBorrowerFieldAction(borrowerId2, Borrower.Fields.address, "500 California St."));

        assertThat(actualActions).usingRecursiveFieldByFieldElementComparator().containsExactlyElementsOf(expectedActions);
    }


    @Test
    public void testParseTasks() {
        List<Task> actualTasks = underTest.parseTasks(sampleTasksPath);
        List<Task> expectedTasks = new ArrayList<>();
        expectedTasks.add(
                Task.builder()
                        .name("Require purchase price for purchase loans")
                        .entity(EntityType.LOAN)
                        .triggerConditions(new Condition[]{
                                Condition.builder()
                                        .field(Loan.Fields.loanAmount)
                                        .comparator(Comparator.EXISTS)
                                        .build(),
                                Condition.builder()
                                        .field(Loan.Fields.loanType)
                                        .comparator(Comparator.EQUALS)
                                        .value("Purchase")
                                        .build()
                        })
                        .completionConditions(new Condition[]{
                                Condition.builder()
                                        .field(Loan.Fields.purchasePrice)
                                        .comparator(Comparator.EXISTS)
                                        .build()
                        })
                        .build());
        expectedTasks.add(
                Task.builder()
                        .name("Require address for borrower")
                        .entity(EntityType.BORROWER)
                        .triggerConditions(new Condition[]{
                                Condition.builder()
                                        .field(Borrower.Fields.firstName)
                                        .comparator(Comparator.EXISTS)
                                        .build(),
                                Condition.builder()
                                        .field(Borrower.Fields.lastName)
                                        .comparator(Comparator.EXISTS)
                                        .build()
                        })
                        .completionConditions(new Condition[]{
                                Condition.builder()
                                        .field(Borrower.Fields.address)
                                        .comparator(Comparator.EXISTS)
                                        .build()
                        })
                        .build());

        assertThat(actualTasks).usingRecursiveFieldByFieldElementComparator().containsExactlyElementsOf(expectedTasks);
    }

}
