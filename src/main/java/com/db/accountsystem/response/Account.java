package com.db.accountsystem.response;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
public class Account {

    private String accountId;
    private BigDecimal balance;

}
