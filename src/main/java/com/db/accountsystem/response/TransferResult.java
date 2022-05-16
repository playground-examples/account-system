package com.db.accountsystem.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferResult {
	
	private String accountFromId;
	private BigDecimal balanceAfterTransfer;

}