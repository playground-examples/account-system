package com.db.accountsystem.mapper.response;

import com.db.accountsystem.response.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountResponseMapper {

    public Account ToResponse(com.db.accountsystem.domain.Account account) {
        return Account.builder().accountId(account.getAccountId()).balance(account.getBalance()).build();
    }
}
