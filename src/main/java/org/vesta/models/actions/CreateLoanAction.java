package org.vesta.models.actions;

import lombok.ToString;
import org.vesta.db.Dao;
import org.vesta.models.Loan;
import org.vesta.models.task.TaskManager;

@ToString(callSuper = true)
public class CreateLoanAction extends Action {
    @ToString.Exclude
    private final Dao<Loan> loanDao;

    public CreateLoanAction(TaskManager taskManager, Dao<Loan> loanDao, String entityId) {
        super(ActionType.CREATE_LOAN, taskManager, entityId);
        this.loanDao = loanDao;
    }

    @Override
    public Object updateEntity() {
        Loan loan = new Loan(this.getEntityId());
        loanDao.insert(loan);
        return loan;
    }
}
