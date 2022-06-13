package org.vesta.models.actions;

import lombok.ToString;
import org.vesta.db.Dao;
import org.vesta.db.TaskManager;
import org.vesta.models.Loan;

@ToString(callSuper = true)
public class SetLoanFieldAction extends Action {
    @ToString.Exclude
    private final Dao<Loan> loanDao;
    private final String field;
    private final Object value;

    public SetLoanFieldAction(TaskManager taskManager,
                              Dao<Loan> loanDao, String entityId, String field, Object value) {
        super(ActionType.SET_LOAN_FIELD, taskManager, entityId);
        this.loanDao = loanDao;
        this.field = field;
        this.value = value;
    }

    @Override
    public Object updateEntity() {
        return loanDao.updateField(this.getEntityId(), field, value);
    }
}
