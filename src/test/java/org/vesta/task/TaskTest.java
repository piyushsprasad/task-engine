package org.vesta.task;

import org.junit.jupiter.api.Test;
import org.vesta.models.Borrower;
import org.vesta.models.Loan;
import org.vesta.models.task.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskTest {

    @Test
    public void testTaskForLoan() {
        Task purchasePriceRequired =
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
                        .build();

        Loan l = new Loan("abc");
        assertEquals(TaskState.CANCELLED, purchasePriceRequired.computeState(l));

        l.setLoanAmount(100);
        l.setLoanType("Purchase");
        assertEquals(TaskState.OPEN, purchasePriceRequired.computeState(l));

        l.setLoanAmount(null);
        assertEquals(TaskState.CANCELLED, purchasePriceRequired.computeState(l));

        l.setLoanAmount(100);
        l.setPurchasePrice(1000);
        assertEquals(TaskState.COMPLETED, purchasePriceRequired.computeState(l));
    }

    @Test
    public void testTaskForBorrower() {
        Task addressRequired =
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
                        .build();
        Borrower b = new Borrower("abc", "def");
        assertEquals(TaskState.CANCELLED, addressRequired.computeState(b));

        b.setFirstName("saul");
        assertEquals(TaskState.CANCELLED, addressRequired.computeState(b));

        b.setLastName("goodman");
        assertEquals(TaskState.OPEN, addressRequired.computeState(b));

        b.setLastName(null);
        assertEquals(TaskState.CANCELLED, addressRequired.computeState(b));

        b.setLastName("gooderman");
        assertEquals(TaskState.OPEN, addressRequired.computeState(b));

        b.setAddress("Wallaby Way, Sydney");
        assertEquals(TaskState.COMPLETED, addressRequired.computeState(b));
    }
}
