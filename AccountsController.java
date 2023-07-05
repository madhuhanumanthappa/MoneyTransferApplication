package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.EmailNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  private EmailNotificationService emailNotificationService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
    this.emailNotificationService = emailNotificationService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }


  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public  String moneyTransferBetweenAccounts(@RequestParam(required = true) String accountFromId, @RequestParam(required = true) String accountToId, @RequestParam(required = true) BigDecimal transferAmountValue) {
    String transferProcessDetails = "";
    ConcurrentHashMap<Account, Account> accountDetailsBeforeTransfer = new ConcurrentHashMap<>();
    ConcurrentHashMap<Account, Account> accountDetailsAfterTransfer = new ConcurrentHashMap<>();
    Account fromAccount = accountsService.getAccount(accountFromId);
    Account toAccount = accountsService.getAccount(accountToId);

    accountDetailsBeforeTransfer.put(fromAccount, toAccount);

    if (accountDetailsBeforeTransfer.size() > 0) {
      if (transferAmountValue.compareTo(BigDecimal.ONE) >= 0)  {
        if (fromAccount.getBalance().compareTo(transferAmountValue) >=0 ) {
          transferProcessDetails = accountDetailsBeforeTransfer.toString();
          accountDetailsAfterTransfer = this.accountsService.transferAmountBetweenAccounts(accountDetailsBeforeTransfer, transferAmountValue);
          // Code to call the Email Notification Service and to send notification for both account holders with a message containing id of other bank account
          // and amount transferred.
          if (accountDetailsAfterTransfer.size() > 0 ) {
            accountDetailsAfterTransfer.entrySet().forEach( entry -> {
              emailNotificationService.notifyAboutTransfer(entry.getKey(), " [" + transferAmountValue.toString() + " - Amount debited from your Account - " + entry.getKey().getAccountId() + ". Now Balance after debit is - " + entry.getKey().getBalance() + " ]");
              emailNotificationService.notifyAboutTransfer(entry.getValue(), " [" + transferAmountValue.toString() + " - Amount credited to your Account - " + entry.getValue().getAccountId()  + ". Now Balance after credit is - " + entry.getValue().getBalance() + " ]");
            });
          }
          transferProcessDetails = accountDetailsAfterTransfer.toString();
        } else {
          transferProcessDetails = "Insufficient Balance in the Bank Account, Overdraft facility is not available for this Bank Account.";
        }
      } else {
        transferProcessDetails = "Minimum Transfer Amount value for Transfer process is  at least 1 or greater than 1.";
      }
    } else {
      transferProcessDetails = "Required From and To Bank Account details for Transfer process is not found.";
    }


    return transferProcessDetails;
  }
}
