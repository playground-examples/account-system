package com.db.accountsystem.domain;

import com.db.accountsystem.utils.DateTimeUtils;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ACCOUNTS")
@Audited
public class Account {

    @NotNull
    @Id
    @Column(name = "ACCOUNTID")
    private String accountId;

    @NotNull
    @Min(value = 0, message = "Initial balance must be positive.")
    @Column(name = "BALANCE")
    private BigDecimal balance;

    @Column(name = "INSERT_TS", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotAudited
    private Date insertTime;

    @Column(name = "MODIFY_TS", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotAudited
    private Date modifyTime;

    @PrePersist
    public void prePersist() {
        insertTime = DateTimeUtils.generateCurrentTimestamp();
    }

    @PreUpdate
    public void preUpdate() {
        modifyTime = DateTimeUtils.generateCurrentTimestamp();
    }

    @PreRemove
    public void preRemove() {
        modifyTime = DateTimeUtils.generateCurrentTimestamp();
    }

}
