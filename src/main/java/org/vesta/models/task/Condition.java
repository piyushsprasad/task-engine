package org.vesta.models.task;

import lombok.*;

import java.lang.reflect.Field;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Condition {
    private String field;
    private Comparator comparator;
    private Object value;

    public boolean meetsCondition(Object o) {
        Object value;
        try {
            Field field = o.getClass().getDeclaredField(this.getField());
            field.setAccessible(true);
            value = field.get(o);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(
                    String.format(
                            "Unable to find or access field %s on object %s for condition %s",
                            this.getField(), o.toString(), this));
        }

        Comparator comparator = this.getComparator();
        switch (comparator) {
            case EQUALS:
                if (value instanceof String) {
                    return ((String) value).equalsIgnoreCase((String) this.getValue());
                }
                return this.getValue().equals(value);
            case EXISTS:
                if (value instanceof String) {
                    return !value.equals("");
                }
                return value != null;
            default:
                throw new IllegalArgumentException("Invalid comparator provided for " + this);
        }
    }
}
