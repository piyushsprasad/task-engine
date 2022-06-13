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
public class Borrower {
    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String address;
    private Integer birthYear;
    private String loanId;

    public Borrower(String id, String loanId) {
        this.id = id;
        this.loanId = loanId;
    }
}
