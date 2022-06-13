package org.vesta.models.task;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@FieldNameConstants
public class TaskInstance {
    @Id
    private String id;
    private String entityId;
    private String taskDefinitionId;
    private String name;
    private TaskState state;

    public TaskInstance(String entityId, String taskDefinitionId, String name, TaskState state) {
        // Composite key of entityId and taskDefinitionId as there can only be one unique type of task per entity.
        this.id = entityId + ":" + taskDefinitionId;
        this.entityId = entityId;
        this.taskDefinitionId = taskDefinitionId;
        this.name = name;
        this.state = state;
    }
}
