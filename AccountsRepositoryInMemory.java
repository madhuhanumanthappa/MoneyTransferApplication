package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

    @Override
    public ConcurrentHashMap<Account, Account> transferAmountBetweenAccounts(ConcurrentHashMap<Account, Account> accountDetailsBeforeTransfer, BigDecimal transferAmountValue){

        ConcurrentHashMap<Account, Account> accountDetailsAfterTransfer = new ConcurrentHashMap<>();

        if ( accountDetailsBeforeTransfer.size() > 0 ) {
            for (ConcurrentHashMap.Entry<Account, Account> transferAccountDetailsEntries : accountDetailsBeforeTransfer.entrySet()) {
                if (transferAmountValue.compareTo(BigDecimal.ONE) >= 0 &&  transferAccountDetailsEntries.getKey().getBalance().compareTo(transferAmountValue) >=0) {
                    transferAccountDetailsEntries.getKey().setBalance(transferAccountDetailsEntries.getKey().getBalance().subtract(transferAmountValue));
                    transferAccountDetailsEntries.getValue().setBalance(transferAccountDetailsEntries.getValue().getBalance().add(transferAmountValue));
                    accountDetailsAfterTransfer.put(transferAccountDetailsEntries.getKey(), transferAccountDetailsEntries.getValue());
                } else {
                    accountDetailsAfterTransfer.put(transferAccountDetailsEntries.getKey(), transferAccountDetailsEntries.getValue());
                }
            }
        }
       return accountDetailsAfterTransfer;
    }
}
