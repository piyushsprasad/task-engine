package org.vesta.models;

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
@FieldNameConstants
@EqualsAndHashCode(callSuper = false)
@Entity
public class Loan {
    @Id
    private String id;
    private Integer loanAmount;
    private String loanType;
    private Integer purchasePrice;
    private String propertyAddress;

    public Loan(String id) {
        this.id = id;
    }
}
