package org.vesta.models.actions;

import lombok.ToString;
import org.vesta.db.Dao;
import org.vesta.models.Borrower;
import org.vesta.models.task.TaskManager;

@ToString(callSuper = true)
public class SetBorrowerFieldAction extends Action {
    @ToString.Exclude
    private final Dao<Borrower> borrowerH2Dao;
    private final String field;
    private final Object value;

    public SetBorrowerFieldAction(TaskManager taskManager, Dao<Borrower> borrowerH2Dao,
                                  String entityId, String field, Object value) {
        super(ActionType.SET_BORROWER_FIELD, taskManager, entityId);
        this.borrowerH2Dao = borrowerH2Dao;
        this.field = field;
        this.value = value;
    }

    @Override
    public Object updateEntity() {
        return borrowerH2Dao.updateField(this.getEntityId(), field, value);
    }
}
