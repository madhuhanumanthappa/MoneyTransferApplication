# MoneyTransferApplication
Creating functionality for a transfer of money between bank accounts.

AccountsRepository.java : In this interface new method transferAmountBetweenAccounts(ConcurrentHashMap<Account, Account> accountDetails, 
BigDecimal transferAmountValue) is added.

AccountsRepositoryInMemory.java : In this class implementation for newly added method transferAmountBetweenAccounts(ConcurrentHashMap<Account, Account> accountDetails, 
BigDecimal transferAmountValue) in the  AccountsRepository interface is provided.

AccountsService.java : In this class new method transferAmountBetweenAccounts(ConcurrentHashMap<Account, Account> accountDetails, BigDecimal transferAmountValue)
is added.

AccountsController.java : In this class new POSTMapping method moneyTransferBetweenAccounts(String accountFromId, String accountToId, BigDecimal transferAmountValue)
is added.

AccountsControllerTest.java : In this test class new test case scenarios are added for the newly added POSTMapping method 
moneyTransferBetweenAccounts(String accountFromId, String accountToId, BigDecimal transferAmountValue) in the AccountsController.java class.

AccountsServiceTest.java :  In this test class new test case scenarios are added for the newly added method 
transferAmountBetweenAccounts(ConcurrentHashMap<Account, Account> accountDetails, BigDecimal transferAmountValue) in the AccountsService.java class.

# Steps to run the application:
1. First download the src.rar file which has complete code  with all the changed files or download the above mentioned java files one by one and replace or overwrite with
   the newly downloaded java class files. (local files with the above mentioned ones).

2. Once the code base has the new java class files then please clean and build the application using Gradle or Maven build tool.

3. Once the build is successful then please run the new test cases which are written for the newly added method
   moneyTransferBetweenAccounts() in the AccountsControllerTest.java and AccountsServiceTest.java.

