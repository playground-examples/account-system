package com.db.accountsystem.mapper.response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AccountResponseMapperTest {

    @InjectMocks
    private AccountResponseMapper accountResponseMapper;

    @Test
    public void testToModel() {
        BigDecimal balance = new BigDecimal(1000);
        String accountId = "Id-123";
        com.db.accountsystem.domain.Account accountDomain =
                com.db.accountsystem.domain.Account.builder().accountId(accountId)
                        .balance(balance).build();
        com.db.accountsystem.response.Account accountToModel = accountResponseMapper.ToResponse(accountDomain);
        assertThat(accountToModel.getAccountId()).isEqualTo(accountId);
        assertThat(accountToModel.getBalance()).isEqualTo(balance);
    }
}
