package org.vesta.task;

import org.junit.jupiter.api.Test;
import org.vesta.models.Borrower;
import org.vesta.models.Loan;
import org.vesta.models.task.Comparator;
import org.vesta.models.task.Condition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConditionTest {

    @Test
    public void testExists() {
        Loan l = new Loan("abc");
        Condition loanAmountExistsCondition =
                Condition.builder()
                        .field(Loan.Fields.loanAmount)
                        .comparator(Comparator.EXISTS)
                        .build();
        assertFalse(loanAmountExistsCondition.meetsCondition(l));

        l.setLoanAmount(100);
        assertTrue(loanAmountExistsCondition.meetsCondition(l));

        Borrower b = new Borrower("borr1", l.getId());
        Condition borrowerFirstNameExistsCondition =
                Condition.builder()
                        .field(Borrower.Fields.firstName)
                        .comparator(Comparator.EXISTS)
                        .build();
        assertFalse(borrowerFirstNameExistsCondition.meetsCondition(b));

        b.setFirstName("Barry");
        assertTrue(borrowerFirstNameExistsCondition.meetsCondition(b));

        // test empty string
        b.setFirstName("");
        assertFalse(borrowerFirstNameExistsCondition.meetsCondition(b));
    }

    @Test
    public void testEqualsForLoan() {
        // test number
        Integer purchasePrice = 100000;
        Condition purchasePriceEqualsCondition =
                Condition.builder()
                        .field(Loan.Fields.purchasePrice)
                        .comparator(Comparator.EQUALS)
                        .value(purchasePrice)
                        .build();
        // test string
        String loanType = "Purchase";
        Condition loanTypeEqualsCondition =
                Condition.builder()
                        .field(Loan.Fields.loanType)
                        .comparator(Comparator.EQUALS)
                        .value(loanType)
                        .build();
        Loan l = new Loan("xyz");
        assertFalse(purchasePriceEqualsCondition.meetsCondition(l));
        assertFalse(loanTypeEqualsCondition.meetsCondition(l));

        l.setPurchasePrice(purchasePrice);
        assertTrue(purchasePriceEqualsCondition.meetsCondition(l));
        assertFalse(loanTypeEqualsCondition.meetsCondition(l));

        l.setLoanType(loanType);
        assertTrue(purchasePriceEqualsCondition.meetsCondition(l));
        assertTrue(loanTypeEqualsCondition.meetsCondition(l));

        // test lower case
        l.setLoanType(loanType.toLowerCase());
        assertTrue(purchasePriceEqualsCondition.meetsCondition(l));
        assertTrue(loanTypeEqualsCondition.meetsCondition(l));

        // null fields
        l.setPurchasePrice(null);
        l.setLoanType(null);
        assertFalse(purchasePriceEqualsCondition.meetsCondition(l));
        assertFalse(loanTypeEqualsCondition.meetsCondition(l));
    }

    @Test
    public void testEqualsForBorrower() {
        // test number
        Integer birthYear = 1995;
        Condition birthYearEqualsCondition =
                Condition.builder()
                        .field(Borrower.Fields.birthYear)
                        .comparator(Comparator.EQUALS)
                        .value(birthYear)
                        .build();
        // test string
        String lastName = "Paperboy";
        Condition borrowerLastNameEqualsCondition =
                Condition.builder()
                        .field(Borrower.Fields.lastName)
                        .comparator(Comparator.EQUALS)
                        .value(lastName)
                        .build();
        Borrower b = new Borrower("borr9", "loan1");
        assertFalse(birthYearEqualsCondition.meetsCondition(b));
        assertFalse(borrowerLastNameEqualsCondition.meetsCondition(b));

        b.setBirthYear(birthYear);
        assertTrue(birthYearEqualsCondition.meetsCondition(b));
        assertFalse(borrowerLastNameEqualsCondition.meetsCondition(b));

        b.setLastName(lastName);
        assertTrue(birthYearEqualsCondition.meetsCondition(b));
        assertTrue(borrowerLastNameEqualsCondition.meetsCondition(b));

        // test lower case
        b.setLastName(lastName.toLowerCase());
        assertTrue(birthYearEqualsCondition.meetsCondition(b));
        assertTrue(borrowerLastNameEqualsCondition.meetsCondition(b));

        // null fields
        b.setBirthYear(null);
        b.setLastName(null);
        assertFalse(birthYearEqualsCondition.meetsCondition(b));
        assertFalse(borrowerLastNameEqualsCondition.meetsCondition(b));
    }
}
