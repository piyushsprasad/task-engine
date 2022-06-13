package org.vesta.models.actions;

public enum ActionType {
    CREATE_LOAN("createLoan"),
    CREATE_BORROWER("createBorrower"),
    SET_LOAN_FIELD("setLoanField"),
    SET_BORROWER_FIELD("setBorrowerField");
    private final String typeName;

    ActionType(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return this.typeName;
    }

    public static ActionType getEnum(String value) {
        for (ActionType type : values()) {
            if (type.typeName.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No ActionType enum available for type: " + value);
    }
}
