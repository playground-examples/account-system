package com.db.accountsystem.service;

import com.db.accountsystem.domain.Account;
import com.db.accountsystem.exception.AccountNotExistException;
import com.db.accountsystem.exception.DuplicateAccountIdException;
import com.db.accountsystem.exception.OverDraftException;
import com.db.accountsystem.exception.SystemException;
import com.db.accountsystem.mapper.request.AccountRequestMapper;
import com.db.accountsystem.mapper.response.AccountResponseMapper;
import com.db.accountsystem.repository.AccountsRepository;
import com.db.accountsystem.request.TransferRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AccountsServiceTest {

    @Mock
    AccountsRepository accountsRepository;
    @Mock
    AccountRequestMapper accountRequestMapper;
    @Mock
    AccountResponseMapper accountResponseMapper;

    @Mock
    NotificationService notificationService;
    @InjectMocks
    private AccountsService accountsService;

    @Test
    public void addAccount() throws Exception {
        //given
        com.db.accountsystem.request.Account account = com.db.accountsystem.request.Account.builder().accountId("Id-123").balance(new BigDecimal(1000)).build();
        when(accountsRepository.findByAccountId(account.getAccountId())).thenReturn(Optional.empty());
        com.db.accountsystem.domain.Account domainAccount = Account.builder().accountId("Id-123").balance(new BigDecimal(1000)).build();
        when(accountRequestMapper.toModel(account)).thenReturn(domainAccount);
        this.accountsService.createAccount(account);

        verify(accountsRepository, times(1)).save(domainAccount);
    }

    @Test
    public void addAccount_failsOnDuplicateId() throws Exception {
        //given
        String uniqueId = "Id-" + System.currentTimeMillis();
        com.db.accountsystem.request.Account account = com.db.accountsystem.request.Account.builder().accountId(uniqueId).balance(new BigDecimal(1000)).build();
        when(accountsRepository.findByAccountId(account.getAccountId())).thenReturn(Optional.of(com.db.accountsystem.domain.Account.builder().accountId("Id-123").balance(new BigDecimal(1000)).build()));
        //when-then
        Exception exception = assertThrows(DuplicateAccountIdException.class, () -> {
            this.accountsService.createAccount(account);
        });
        assertThat(exception.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");

    }

    @Test
    public void testOverdraftBalance() throws OverDraftException, AccountNotExistException, SystemException {
        String accountFromId = "1";
        String accountFromTo = "2";
        BigDecimal amount = new BigDecimal(20);

        TransferRequest request = new TransferRequest();
        request.setAccountFromId(accountFromId);
        request.setAccountToId(accountFromTo);
        request.setAmount(amount);

        com.db.accountsystem.domain.Account accFrom = com.db.accountsystem.domain.Account.builder().accountId(accountFromId).balance(BigDecimal.TEN).build();
        com.db.accountsystem.domain.Account accTo = com.db.accountsystem.domain.Account.builder().accountId(accountFromTo).balance(BigDecimal.TEN).build();

        when(accountsRepository.getAccountForUpdate(accountFromId)).thenReturn(Optional.of(accFrom));
        when(accountsRepository.getAccountForUpdate(accountFromTo)).thenReturn(Optional.of(accTo));

        Exception exception = assertThrows(OverDraftException.class, () -> {
            accountsService.transferBalances(request);
        });

        assertThat(exception.getMessage()).isEqualTo("Account with id:" + accFrom.getAccountId() + " does not have enough balance to transfer.");

    }

    @Test
    public void testRetrieveBalance() {
        Account domainAccount = Account.builder().accountId("1").balance(BigDecimal.ONE).build();
        when(accountsRepository.findByAccountId("1")).thenReturn(Optional.of(domainAccount));
        com.db.accountsystem.response.Account responseAccount = com.db.accountsystem.response.Account.builder().accountId("1").balance(BigDecimal.ONE).build();
        when(accountResponseMapper.ToResponse(domainAccount)).thenReturn(responseAccount);
        com.db.accountsystem.response.Account expectedResponse = accountsService.getAccount("1");
        assertEquals(BigDecimal.ONE, expectedResponse.getBalance());
    }

    @Test
    public void testTransferBalance() throws Exception {
        String accountFromId = "1";
        String accountFromTo = "2";
        BigDecimal amount = new BigDecimal(10);

        TransferRequest request = new TransferRequest();
        request.setAccountFromId(accountFromId);
        request.setAccountToId(accountFromTo);
        request.setAmount(amount);

        com.db.accountsystem.domain.Account accFrom = com.db.accountsystem.domain.Account.builder().accountId(accountFromId).balance(BigDecimal.TEN).build();
        com.db.accountsystem.domain.Account accTo = com.db.accountsystem.domain.Account.builder().accountId(accountFromId).balance(BigDecimal.TEN).build();

        when(accountsRepository.getAccountForUpdate(accountFromId)).thenReturn(Optional.of(accFrom));
        when(accountsRepository.getAccountForUpdate(accountFromTo)).thenReturn(Optional.of(accTo));

        accountsService.transferBalances(request);
        verify(accountsRepository, times(1)).save(accFrom);
        verify(accountsRepository, times(1)).save(accTo);
        verify(notificationService, times(1)).notifyAboutTransfer(accFrom, String.format("Amount %s Rs. is successfully debited to %s Account. Balance after transfer is %s Rs only."
                , request.getAmount(), accTo.getAccountId(), accFrom.getBalance().subtract(request.getAmount())));
        verify(notificationService, times(1)).notifyAboutTransfer(accTo, String.format("Amount %s Rs. is Credited to your account from %s account. Balance after credit is %s Rs only."
                , request.getAmount(), accTo.getAccountId(), accTo.getBalance().add(request.getAmount())));
    }

}
