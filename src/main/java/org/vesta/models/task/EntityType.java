package org.vesta.models.task;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EntityType {
    LOAN("Loan"),
    BORROWER("Borrower");

    private final String typeName;

    EntityType(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return this.typeName;
    }

    @JsonValue
    public String getTypeName() {
        return typeName;
    }

    public static EntityType getEnum(String value) {
        for (EntityType type : values()) {
            if (type.typeName.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No EntityType enum available for type: " + value);
    }
}
