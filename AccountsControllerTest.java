package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }

  @Test
  void transferAmountWithZeroAmount  throws Exception {
    Account fromAccount = new Account("Id-123", new BigDecimal("5000"));
    this.accountsService.createAccount(fromAccount);
    Account toAccount = new Account("ID-456", new BigDecimal("6000"));
    this.accountsService.createAccount(toAccount);
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"Id-123\", \"toAccountId\" :\"Id-456\", \"transferAmountValue\":0}"))
            .andExpect("Minimum Transfer Amount value for Transfer process is  at least 1 or greater than 1.");
  }

  @Test
  void transferAmountWithNegativeAmount  throws Exception {
    Account fromAccount = new Account("Id-123", new BigDecimal("5000"));
    this.accountsService.createAccount(fromAccount);
    Account toAccount = new Account("ID-456", new BigDecimal("6000"));
    this.accountsService.createAccount(toAccount);
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"Id-123\", \"toAccountId\" :\"Id-456\", \"transferAmountValue\":-1}"))
            .andExpect("Minimum Transfer Amount value for Transfer process is  at least 1 or greater than 1.");
  }

  @Test
  void transferAmountWithGreaterAmount  throws Exception {
    Account fromAccount = new Account("Id-123", new BigDecimal("5000"));
    this.accountsService.createAccount(fromAccount);
    Account toAccount = new Account("ID-456", new BigDecimal("6000"));
    this.accountsService.createAccount(toAccount);
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"Id-123\", \"toAccountId\" :\"Id-456\", \"transferAmountValue\":5001}"))
            .andExpect("Insufficient Balance in the Bank Account, Overdraft facility is not available for this Bank Account.");
  }

  @Test
  void transferAmountBetweenAccounts  throws Exception {
    Account fromAccount = new Account("Id-123", new BigDecimal("5000"));
    this.accountsService.createAccount(fromAccount);
    Account toAccount = new Account("ID-456", new BigDecimal("6000"));
    this.accountsService.createAccount(toAccount);
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"Id-123\", \"toAccountId\" :\"Id-456\", \"transferAmountValue\":4999}"))
            .andExpect(content().string("{\"fromAccountId\":\"Id-123\", \"balance\":\"1\", \"toAccountId\":\"Id-456\", \"balance\":\"10999}"));

    Account fromAccountAfterTransfer = accountsService.getAccount("Id-123");
    Account toAccountAfterTransfer = accountsService.getAccount("ID-456");

    assertThat(fromAccountAfterTransfer.getAccountId()).isEqualTo("Id-123");
    assertThat(toAccountAfterTransfer.getAccountId()).isEqualTo("ID-456");
    assertThat(fromAccountAfterTransfer.getBalance()).isEqualByComparingTo("1");
    assertThat(toAccountAfterTransfer.getBalance()).isEqualByComparingTo("10999");
  }

  @Test
  void transferAmountBetweenAccountsWithEqualAmount  throws Exception {
    Account fromAccount = new Account("Id-123", new BigDecimal("5000"));
    this.accountsService.createAccount(fromAccount);
    Account toAccount = new Account("ID-456", new BigDecimal("6000"));
    this.accountsService.createAccount(toAccount);
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"Id-123\", \"toAccountId\" :\"Id-456\", \"transferAmountValue\":5000}"))
            .andExpect(content().string("{\"fromAccountId\":\"Id-123\", \"balance\":\"0\", \"toAccountId\":\"Id-456\", \"balance\":\"11000}"));

    Account fromAccountAfterTransfer = accountsService.getAccount("Id-123");
    Account toAccountAfterTransfer = accountsService.getAccount("ID-456");

    assertThat(fromAccountAfterTransfer.getAccountId()).isEqualTo("Id-123");
    assertThat(toAccountAfterTransfer.getAccountId()).isEqualTo("ID-456");
    assertThat(fromAccountAfterTransfer.getBalance()).isEqualByComparingTo("0");
    assertThat(toAccountAfterTransfer.getBalance()).isEqualByComparingTo("11000");
  }

}
