package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  void transferAmountWithZeroAmount {
    ConcurrentHashMap<Account, Account> accountDetailsBeforeTransfer = new ConcurrentHashMap<>();
    Account fromAccount = new Account("Id-123", new BigDecimal(5000));
    this.accountsService.createAccount(fromAccount);
    Account toAccount = new Account("ID-456", new BigDecimal(6000));
    this.accountsService.createAccount(toAccount);
    accountDetailsBeforeTransfer.put(fromAccount, toAccount);
    ConcurrentHashMap<Account, Account> accountDetailsAfterTransfer = new ConcurrentHashMap<>();
    accountDetailsAfterTransfer = this.accountsService.transferAmountBetweenAccounts(accountDetailsBeforeTransfer, new BigDecimal(0));

    for (ConcurrentHashMap.Entry<Account, Account> afterTransferAccountDetailsEntries : accountDetailsAfterTransfer.entrySet()) {
      assertThat(accountDetailsBeforeTransfer.getKey().getAccountId().isEqualTo(afterTransferAccountDetailsEntries.getKey().getAccountId()));
      assertThat(accountDetailsBeforeTransfer.getKey().getBalance().isEqualByComparingTo(afterTransferAccountDetailsEntries.getKey().getBalance()));
      assertThat(accountDetailsBeforeTransfer.getValue().getAccountId().isEqualTo(afterTransferAccountDetailsEntries.getValue().getAccountId()));
      assertThat(accountDetailsBeforeTransfer.getValue().getBalance().isEqualByComparingTo(afterTransferAccountDetailsEntries.getValue().getBalance()));
    }
  }

  @Test
  void transferAmountWithNegativeAmount {
    ConcurrentHashMap<Account, Account> accountDetailsBeforeTransfer = new ConcurrentHashMap<>();
    Account fromAccount = new Account("Id-123", new BigDecimal(5000));
    this.accountsService.createAccount(fromAccount);
    Account toAccount = new Account("ID-456", new BigDecimal(6000));
    this.accountsService.createAccount(toAccount);
    accountDetailsBeforeTransfer.put(fromAccount, toAccount);
    ConcurrentHashMap<Account, Account> accountDetailsAfterTransfer = new ConcurrentHashMap<>();
    accountDetailsAfterTransfer = this.accountsService.transferAmountBetweenAccounts(accountDetailsBeforeTransfer, new BigDecimal(-10));

    for (ConcurrentHashMap.Entry<Account, Account> afterTransferAccountDetailsEntries : accountDetailsAfterTransfer.entrySet()) {
      assertThat(accountDetailsBeforeTransfer.getKey().getAccountId().isEqualTo(afterTransferAccountDetailsEntries.getKey().getAccountId()));
      assertThat(accountDetailsBeforeTransfer.getKey().getBalance().isEqualByComparingTo(afterTransferAccountDetailsEntries.getKey().getBalance()));
      assertThat(accountDetailsBeforeTransfer.getValue().getAccountId().isEqualTo(afterTransferAccountDetailsEntries.getValue().getAccountId()));
      assertThat(accountDetailsBeforeTransfer.getValue().getBalance().isEqualByComparingTo(afterTransferAccountDetailsEntries.getValue().getBalance()));
    }
  }

  @Test
  void transferAmountWithGreaterAmount  {
    ConcurrentHashMap<Account, Account> accountDetailsBeforeTransfer = new ConcurrentHashMap<>();
    Account fromAccount = new Account("Id-123", new BigDecimal(5000));
    this.accountsService.createAccount(fromAccount);
    Account toAccount = new Account("ID-456", new BigDecimal(6000));
    this.accountsService.createAccount(toAccount);
    accountDetailsBeforeTransfer.put(fromAccount, toAccount);
    ConcurrentHashMap<Account, Account> accountDetailsAfterTransfer = new ConcurrentHashMap<>();
    accountDetailsAfterTransfer = this.accountsService.transferAmountBetweenAccounts(accountDetailsBeforeTransfer, new BigDecimal(5001));

    for (ConcurrentHashMap.Entry<Account, Account> afterTransferAccountDetailsEntries : accountDetailsAfterTransfer.entrySet()) {
      assertThat(accountDetailsBeforeTransfer.getKey().getAccountId().isEqualTo(afterTransferAccountDetailsEntries.getKey().getAccountId()));
      assertThat(accountDetailsBeforeTransfer.getKey().getBalance().isEqualByComparingTo(afterTransferAccountDetailsEntries.getKey().getBalance()));
      assertThat(accountDetailsBeforeTransfer.getValue().getAccountId().isEqualTo(afterTransferAccountDetailsEntries.getValue().getAccountId()));
      assertThat(accountDetailsBeforeTransfer.getValue().getBalance().isEqualByComparingTo(afterTransferAccountDetailsEntries.getValue().getBalance()));
    }
  }

  @Test
  void transferAmountBetweenAccounts {
    ConcurrentHashMap<Account, Account> accountDetailsBeforeTransfer = new ConcurrentHashMap<>();
    Account fromAccount = new Account("Id-123", new BigDecimal(5000));
    this.accountsService.createAccount(fromAccount);
    Account toAccount = new Account("ID-456", new BigDecimal(6000));
    this.accountsService.createAccount(toAccount);
    accountDetailsBeforeTransfer.put(fromAccount, toAccount);
    ConcurrentHashMap<Account, Account> accountDetailsAfterTransfer = new ConcurrentHashMap<>();
    accountDetailsAfterTransfer = this.accountsService.transferAmountBetweenAccounts(accountDetailsBeforeTransfer, new BigDecimal(4999));

    for (ConcurrentHashMap.Entry<Account, Account> afterTransferAccountDetailsEntries : accountDetailsAfterTransfer.entrySet()) {
      assertThat(accountDetailsBeforeTransfer.getKey().getAccountId().isEqualTo(afterTransferAccountDetailsEntries.getKey().getAccountId()));
      assertThat(new BigDecimal(1).isEqualByComparingTo(afterTransferAccountDetailsEntries.getKey().getBalance()));
      assertThat(accountDetailsBeforeTransfer.getValue().getAccountId().isEqualTo(afterTransferAccountDetailsEntries.getValue().getAccountId()));
      assertThat(new BigDecimal(10999).isEqualByComparingTo(afterTransferAccountDetailsEntries.getValue().getBalance()));
    }
  }

  @Test
  void transferAmountBetweenAccountsWithEqualAmount {
    ConcurrentHashMap<Account, Account> accountDetailsBeforeTransfer = new ConcurrentHashMap<>();
    Account fromAccount = new Account("Id-123", new BigDecimal(5000));
    this.accountsService.createAccount(fromAccount);
    Account toAccount = new Account("ID-456", new BigDecimal(6000));
    this.accountsService.createAccount(toAccount);
    accountDetailsBeforeTransfer.put(fromAccount, toAccount);
    ConcurrentHashMap<Account, Account> accountDetailsAfterTransfer = new ConcurrentHashMap<>();
    accountDetailsAfterTransfer = this.accountsService.transferAmountBetweenAccounts(accountDetailsBeforeTransfer, new BigDecimal(5000));

    for (ConcurrentHashMap.Entry<Account, Account> afterTransferAccountDetailsEntries : accountDetailsAfterTransfer.entrySet()) {
      assertThat(accountDetailsBeforeTransfer.getKey().getAccountId().isEqualTo(afterTransferAccountDetailsEntries.getKey().getAccountId()));
      assertThat(new BigDecimal(0).isEqualByComparingTo(afterTransferAccountDetailsEntries.getKey().getBalance()));
      assertThat(accountDetailsBeforeTransfer.getValue().getAccountId().isEqualTo(afterTransferAccountDetailsEntries.getValue().getAccountId()));
      assertThat(new BigDecimal(11000).isEqualByComparingTo(afterTransferAccountDetailsEntries.getValue().getBalance()));
    }
  }

}
