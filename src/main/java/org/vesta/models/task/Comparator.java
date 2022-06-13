package org.vesta.models.task;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Comparator {
    EQUALS,
    EXISTS;

    @JsonValue
    public String getTypeName() {
        return this.name().toLowerCase();
    }
}
