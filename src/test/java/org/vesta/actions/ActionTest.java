package org.vesta.actions;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vesta.db.Dao;
import org.vesta.db.HibernateConnection;
import org.vesta.db.TaskManager;
import org.vesta.db.impl.H2DaoImpl;
import org.vesta.db.impl.TaskMapDaoImpl;
import org.vesta.models.Borrower;
import org.vesta.models.Loan;
import org.vesta.models.actions.*;
import org.vesta.models.task.Task;
import org.vesta.models.task.TaskInstance;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class ActionTest {
    private HibernateConnection h;
    private Dao<Loan> h2LoanDao;
    private Dao<Borrower> h2BorrowerDao;

    private Dao<Task> taskDao;
    private Dao<TaskInstance> h2TaskInstanceDao;

    private ActionFactory actionFactory;


    @BeforeEach
    public void setup() {
        h = new HibernateConnection();
        h2LoanDao = new H2DaoImpl<Loan>(h, Loan.class);
        h2BorrowerDao = new H2DaoImpl<Borrower>(h, Borrower.class);
        taskDao = new TaskMapDaoImpl();
        h2TaskInstanceDao = new H2DaoImpl<TaskInstance>(h, TaskInstance.class);
        TaskManager taskManager = new TaskManager(taskDao, h2TaskInstanceDao);
        actionFactory = new ActionFactory(h2LoanDao, h2BorrowerDao, taskManager);
    }

    @AfterEach
    public void shutdown() {
        h.shutdown();
    }

    @Test
    public void testCreateUpdateLoan() {
        // validate loan created
        String loanId = "loanId";
        CreateLoanAction createLoanAction = actionFactory.createLoanAction(loanId);
        createLoanAction.process();

        Optional<Loan> possibleLoan = h2LoanDao.get(loanId);
        Loan expectedLoan = new Loan(loanId);
        assertEquals(expectedLoan, possibleLoan.get());

        // validate loan modified
        Integer loanAmount = 100;
        SetLoanFieldAction setLoanFieldAction =
                actionFactory.setLoanFieldAction(loanId, Loan.Fields.loanAmount, loanAmount);
        setLoanFieldAction.process();

        expectedLoan.setLoanAmount(loanAmount);
        possibleLoan = h2LoanDao.get(loanId);
        assertEquals(expectedLoan, possibleLoan.get());

        // validate field nulled
        setLoanFieldAction = actionFactory.setLoanFieldAction(loanId, Loan.Fields.loanAmount, null);
        setLoanFieldAction.process();

        expectedLoan.setLoanAmount(null);
        possibleLoan = h2LoanDao.get(loanId);
        assertEquals(expectedLoan, possibleLoan.get());
    }

    @Test
    public void testCreateUpdateBorrower() {
        // create loan
        String loanId = "loanId";
        CreateLoanAction createLoanAction = actionFactory.createLoanAction(loanId);
        createLoanAction.process();

        // create borrower
        String borrowerId = "borrowerId";
        CreateBorrowerAction createBorrowerAction = actionFactory.createBorrowerAction(borrowerId, loanId);
        createBorrowerAction.process();

        Optional<Borrower> possibleBorrower = h2BorrowerDao.get(borrowerId);
        Borrower expectedBorrower = new Borrower(borrowerId, loanId);
        assertEquals(expectedBorrower, possibleBorrower.get());

        // update borrower
        String firstName = "bojack";
        SetBorrowerFieldAction setBorrowerFieldAction =
                actionFactory.setBorrowerFieldAction(borrowerId, Borrower.Fields.firstName, firstName);
        setBorrowerFieldAction.process();

        possibleBorrower = h2BorrowerDao.get(borrowerId);
        expectedBorrower.setFirstName(firstName);
        assertEquals(expectedBorrower, possibleBorrower.get());

        // validate field nulled
        setBorrowerFieldAction =
                actionFactory.setBorrowerFieldAction(borrowerId, Borrower.Fields.firstName, null);
        setBorrowerFieldAction.process();

        possibleBorrower = h2BorrowerDao.get(borrowerId);
        expectedBorrower.setFirstName(null);
        assertEquals(expectedBorrower, possibleBorrower.get());

        // create second borrower
        String borrowerId2 = "borrowerId2";
        createBorrowerAction = actionFactory.createBorrowerAction(borrowerId2, loanId);
        createBorrowerAction.process();

        // get all borrowers with loan Id
        List<Borrower> borrowerList = h2BorrowerDao.getByField(Borrower.Fields.loanId, loanId);
        Borrower expectedBorrower2 = new Borrower(borrowerId2, loanId);
        List<Borrower> expectedList = Arrays.asList(expectedBorrower, expectedBorrower2);
        assertIterableEquals(expectedList, borrowerList);
    }

}
