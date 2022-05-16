package com.db.accountsystem.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotNull
    @ApiModelProperty(required = true)
    private String accountFromId;

    @NotNull
    @ApiModelProperty(required = true)
    private String accountToId;

    @NotNull
    @ApiModelProperty(required = true)
    @Min(value = 0, message = "Transfer amount can not be less than zero")
    private BigDecimal amount;

    @JsonCreator
    public TransferRequest(@NotNull @JsonProperty("accountFromId") String accountFromId,
                           @NotNull @JsonProperty("accountToId") String accountToId,
                           @NotNull @Min(value = 0, message = "Transfer amount can not be less than zero") @JsonProperty("amount") BigDecimal amount) {
        super();
        this.accountFromId = accountFromId;
        this.accountToId = accountToId;
        this.amount = amount;
    }

    @JsonCreator
    public TransferRequest() {
        super();
    }

}