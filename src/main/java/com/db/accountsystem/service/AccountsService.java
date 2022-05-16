package com.db.accountsystem.service;

import com.db.accountsystem.constants.ErrorCode;
import com.db.accountsystem.domain.Account;
import com.db.accountsystem.exception.*;
import com.db.accountsystem.mapper.request.AccountRequestMapper;
import com.db.accountsystem.mapper.response.AccountResponseMapper;
import com.db.accountsystem.repository.AccountsRepository;
import com.db.accountsystem.request.TransferRequest;
import com.google.common.annotations.VisibleForTesting;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class AccountsService {

    @Autowired
    private final AccountsRepository accountsRepository;
    @Autowired
    private final AccountRequestMapper accountRequestMapper;
    @Autowired
    private final AccountResponseMapper accountResponseMapper;

    @Autowired
    private final NotificationService notificationService;

    public void createAccount(com.db.accountsystem.request.Account account) throws DuplicateAccountIdException {
        Optional<Account> presentAccount = accountsRepository.findByAccountId(account.getAccountId());
        if (!presentAccount.isPresent()) {
            this.accountsRepository.save(accountRequestMapper.toModel(account));
        } else {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    public com.db.accountsystem.response.Account getAccount(String accountId) {
        Account account = accountsRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AccountNotExistException("Account with id:" + accountId + " does not exist.", ErrorCode.ACCOUNT_ERROR, HttpStatus.NOT_FOUND));
        return accountResponseMapper.ToResponse(account);
    }

    @Transactional
    public void transferBalances(TransferRequest transfer) throws OverDraftException, AccountNotExistException, SystemException {
        Account accountFrom = accountsRepository.getAccountForUpdate(transfer.getAccountFromId())
                .orElseThrow(() -> new AccountNotExistException("Account with id:" + transfer.getAccountFromId() + " does not exist.", ErrorCode.ACCOUNT_ERROR));

        Account accountTo = accountsRepository.getAccountForUpdate(transfer.getAccountToId())
                .orElseThrow(() -> new AccountNotExistException("Account with id:" + transfer.getAccountToId() + " does not exist.", ErrorCode.ACCOUNT_ERROR));

        if (accountFrom.getBalance().compareTo(transfer.getAmount()) < 0) {
            throw new OverDraftException("Account with id:" + accountFrom.getAccountId() + " does not have enough balance to transfer.", ErrorCode.ACCOUNT_ERROR);
        }

        accountFrom.setBalance(accountFrom.getBalance().subtract(transfer.getAmount()));
        this.accountsRepository.save(accountFrom);
        notificationService.notifyAboutTransfer(accountFrom,String.format("Amount %s Rs. is successfully debited to %s Account. Balance after transfer is %s Rs only."
                ,transfer.getAmount(),accountTo.getAccountId(),accountFrom.getBalance().subtract(transfer.getAmount())));
        accountTo.setBalance(accountTo.getBalance().add(transfer.getAmount()));
        this.accountsRepository.save(accountTo);
        notificationService.notifyAboutTransfer(accountTo,String.format("Amount %s Rs. is Credited to your account from %s account. Balance after credit is %s Rs only."
                ,transfer.getAmount(),accountTo.getAccountId(),accountTo.getBalance().add(transfer.getAmount())));
    }

    public BigDecimal checkBalance(String accountId) throws SystemException {
        try {
            log.info("checking balance for id :{} " + accountId);

            Account account = accountsRepository.getAccountForUpdate(accountId)
                    .orElseThrow(() -> new AccountNotExistException("Account with id:" + accountId + " does not exist.", ErrorCode.ACCOUNT_ERROR));

            return account.getBalance();

        } catch (ResourceAccessException ex) {
            final String errorMessage = "Encounter timeout error, please check with system administrator.";

            if (ex.getCause() instanceof SocketTimeoutException) {
                throw new CheckBalanceException(errorMessage, ErrorCode.TIMEOUT_ERROR);
            }
        }
        // for any other fail cases
        throw new SystemException("Encounter internal server error, please check with system administrator.", ErrorCode.SYSTEM_ERROR);
    }

    //@VisibleForTesting
    public void removeAll(){
        this.accountsRepository.deleteAll();
    }

}
