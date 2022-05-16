package com.db.accountsystem.mapper.request;

import com.db.accountsystem.domain.Account;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AccountRequestMapperTest {

    @InjectMocks
    private AccountRequestMapper accountRequestMapper;

    @Test
    public void testToModel() {
        BigDecimal balance = new BigDecimal(1000);
        String accountId = "Id-123";
        com.db.accountsystem.request.Account accountRequest =
                com.db.accountsystem.request.Account.builder().accountId(accountId)
                        .balance(balance).build();
        Account accountToModel = accountRequestMapper.toModel(accountRequest);
        assertThat(accountToModel.getAccountId()).isEqualTo(accountId);
        assertThat(accountToModel.getBalance()).isEqualTo(balance);
    }
}
