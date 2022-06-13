package org.vesta.models.task;

import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@FieldNameConstants
public class Task {
    private String id;
    private String name;
    private EntityType entity;
    private Condition[] triggerConditions;
    private Condition[] completionConditions;

    public TaskState computeState(Object o) {
        // Never open if empty set of trigger conditions.
        if (triggerConditions == null || triggerConditions.length == 0) {
            return TaskState.CANCELLED;
        }

        for (Condition triggerCondition : triggerConditions) {
            if (!triggerCondition.meetsCondition(o)) {
                return TaskState.CANCELLED;
            }
        }

        // Never complete if empty set of completion conditions.
        if (completionConditions == null || completionConditions.length == 0) {
            return TaskState.OPEN;
        }

        for (Condition completionCondition : completionConditions) {
            if (!completionCondition.meetsCondition(o)) {
                return TaskState.OPEN;
            }
        }
        return TaskState.COMPLETED;
    }
}
