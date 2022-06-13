package org.vesta.models.actions;

import org.vesta.db.Dao;
import org.vesta.models.Borrower;
import org.vesta.models.Loan;
import org.vesta.models.task.TaskManager;

public class ActionFactory {
    private final Dao<Loan> loanDao;
    private final Dao<Borrower> borrowerDao;
    private final TaskManager taskManager;

    public ActionFactory(
            Dao<Loan> loanDao, Dao<Borrower> borrowerDao, TaskManager taskManager) {
        this.loanDao = loanDao;
        this.borrowerDao = borrowerDao;
        this.taskManager = taskManager;
    }

    public CreateLoanAction createLoanAction(String loanId) {
        return new CreateLoanAction(taskManager, loanDao, loanId);
    }

    public CreateBorrowerAction createBorrowerAction(String borrowerId, String loanId) {
        return new CreateBorrowerAction(taskManager, borrowerDao, loanDao, borrowerId, loanId);
    }

    public SetLoanFieldAction setLoanFieldAction(String loanId, String field, Object value) {
        return new SetLoanFieldAction(taskManager, loanDao, loanId, field, value);
    }

    public SetBorrowerFieldAction setBorrowerFieldAction(String borrowerId, String field, Object value) {
        return new SetBorrowerFieldAction(taskManager, borrowerDao, borrowerId, field, value);
    }
}
