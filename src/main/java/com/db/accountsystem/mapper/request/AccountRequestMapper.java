package com.db.accountsystem.mapper.request;

import com.db.accountsystem.domain.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountRequestMapper {
    public Account toModel(com.db.accountsystem.request.Account account) {
        return Account.builder().accountId(account.getAccountId())
                .balance(account.getBalance()).build();
    }
}
