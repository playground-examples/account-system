package com.db.accountsystem.controller;

import com.db.accountsystem.exception.AccountNotExistException;
import com.db.accountsystem.exception.CheckBalanceException;
import com.db.accountsystem.exception.OverDraftException;
import com.db.accountsystem.request.TransferRequest;
import com.db.accountsystem.response.TransferResult;
import com.db.accountsystem.service.AccountsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/transaction")
@Api(tags = {"Transaction Controller"}, description = "Provide APIs for transaction related operation")
@Slf4j
public class AccountTransactionsController {

    @Autowired
    private AccountsService accountService;

    @PostMapping(consumes = {"application/json"})
    @ApiOperation(value = "API to create transaction", response = TransferResult.class, produces = "application/json")
    public ResponseEntity transferMoney(@RequestBody @Valid TransferRequest request) throws Exception {

        try {
            accountService.transferBalances(request);

            TransferResult result = new TransferResult();
            result.setAccountFromId(request.getAccountFromId());
            result.setBalanceAfterTransfer(accountService.checkBalance(request.getAccountFromId()));

            return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
        } catch (AccountNotExistException | OverDraftException exception) {
            log.error("Fail to transfer balances, please check with system administrator.");
            throw exception;
        } catch (CheckBalanceException checkBalanceException) {
            log.error("Fail to check balances after transfer, please check with system administrator.");
            throw checkBalanceException;
        }
    }

}
