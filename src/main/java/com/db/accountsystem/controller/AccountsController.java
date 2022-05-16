package com.db.accountsystem.controller;

import com.db.accountsystem.exception.DuplicateAccountIdException;
import com.db.accountsystem.exception.SystemException;
import com.db.accountsystem.request.Account;
import com.db.accountsystem.service.AccountsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/accounts")
@AllArgsConstructor
@Slf4j
@Validated
public class AccountsController {

    @Autowired
    private final AccountsService accountsService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
        log.info("Creating account {}", account);
        try {
            this.accountsService.createAccount(account);
        } catch (DuplicateAccountIdException daie) {
            return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(path = "/{accountId}")
    public com.db.accountsystem.response.Account getAccount(@PathVariable String accountId) {
        log.info("Retrieving account for id {}", accountId);
        return this.accountsService.getAccount(accountId);
    }

    @GetMapping(path = "/balance/{accountId}")
    public BigDecimal checkBalance(@PathVariable String accountId) throws SystemException {
        log.info("Retrieving balance for account id {}", accountId);
        return this.accountsService.checkBalance(accountId);
    }
}
