package org.vesta.models.actions;

import lombok.ToString;
import org.vesta.db.Dao;
import org.vesta.models.Borrower;
import org.vesta.models.Loan;
import org.vesta.models.task.TaskManager;

import java.util.Optional;

@ToString(callSuper = true)
public class CreateBorrowerAction extends Action {
    @ToString.Exclude
    private final Dao<Borrower> borrowerDao;
    @ToString.Exclude
    private final Dao<Loan> loanDao;
    private final String loanId;

    public CreateBorrowerAction(
            TaskManager taskManager, Dao<Borrower> borrowerDao,
            Dao<Loan> loanDao, String borrowerId, String loanId) {
        super(ActionType.CREATE_BORROWER, taskManager, borrowerId);
        this.borrowerDao = borrowerDao;
        this.loanDao = loanDao;
        this.loanId = loanId;
    }

    @Override
    public Object updateEntity() {
        // Validate that loan exists
        Optional<Loan> possibleLoan = loanDao.get(loanId);
        if (possibleLoan.isEmpty()) {
            throw new IllegalArgumentException("Attempting to create borrower for non-existent loan.");
        }

        Borrower borrower = new Borrower(this.getEntityId(), loanId);
        borrowerDao.insert(borrower);
        return borrower;
    }
}
