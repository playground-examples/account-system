package com.db.accountsystem.service;


import com.db.accountsystem.domain.Account;

public interface NotificationService {

  void notifyAboutTransfer(Account account, String transferDescription);
}
