package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

public interface AccountsRepository {

  void createAccount(Account account) throws DuplicateAccountIdException;

  Account getAccount(String accountId);


  ConcurrentHashMap<Account, Account> transferAmountBetweenAccounts(ConcurrentHashMap<Account, Account> accountDetails, BigDecimal transferAmountValue);

  void clearAccounts();
}
