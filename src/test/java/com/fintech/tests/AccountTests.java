package com.fintech.tests;

import com.fintech.pages.AccountPage;
import com.fintech.pages.LoginPage;
import com.fintech.pages.TransferPage;
import com.fintech.pages.TransactionHistoryPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AccountTests extends BaseTest {
	
  private WebDriverWait wait; 
  
  public void loginAsJohn() {
	    driver.get("https://parabank.parasoft.com/parabank/index.htm");

	    LoginPage login = new LoginPage(driver);
	    login.enterUsername("john");
	    login.enterPassword("demo");
	    login.clickLogin();

	    wait = new WebDriverWait(driver, Duration.ofSeconds(15)); // ✅ Initialize wait
  }
  
  private String openAnyAccount(AccountPage ap, String... preferredTypes) {
	    for (String type : preferredTypes) {
	        try {
	            ap.openAccountByName(type);
	            System.out.println("Opened account: " + type);
	            test.info("Opened account: " + type);
	            return type;
	        } catch (AssertionError ignored) {}
	    }
	    throw new AssertionError("No preferred account types found: " + java.util.Arrays.toString(preferredTypes));
	}
  

  @DataProvider(name = "accountTypes")
  public Object[][] accountTypes() {
    return new Object[][] {
      {"Checking"},
      {"Savings"}
    };
  }

  @DataProvider(name = "transferData")
  public Object[][] transferData() {
    return new Object[][] {
      {"Checking", "Savings", 100.00},
      {"Savings", "Checking", 50.00}
    };
  }

  @DataProvider(name = "dateRanges")
  public Object[][] dateRanges() {
    return new Object[][] {
      {"09/01/2025", "09/30/2025"},
      {"08/01/2025", "08/31/2025"}
    };
  }

  @DataProvider(name = "sortFields")
  public Object[][] sortFields() {
    return new Object[][] {
      {"Date", "Ascending"},
      {"Amount", "Descending"}
    };
  }

  @Test(dataProvider = "accountTypes")
  public void viewAccountSummary(String accountType) {
    loginAsJohn();
    AccountPage ap = new AccountPage(driver);
    Assert.assertTrue(ap.isLoaded(), "Accounts overview should be loaded");
    boolean match = ap.getAccountNames().stream()
    	    .anyMatch(name -> name.toLowerCase().contains(accountType.toLowerCase()));
    	Assert.assertTrue(match, accountType + " should be listed");

    ap.openAccountByName(accountType);
    Assert.assertTrue(ap.isBalanceVisible(), "Balance should be visible for " + accountType);
    test.pass(accountType + " account summary validated");
  }

  @Test(dataProvider = "transferData")
  public void validateBalanceAfterTransaction(String from, String to, double amount) {
    loginAsJohn();
    AccountPage ap = new AccountPage(driver);
    if (!ap.getAccountNames().contains(from)) {
        throw new AssertionError("Account '" + from + "' not found — cannot proceed");
    }
    double before = ap.getBalance(from);

    ap.transferFunds(from, to, amount);
    TransferPage tp = new TransferPage(driver);
    Assert.assertTrue(tp.isTransferConfirmed(), "Transfer confirmation missing");

    driver.navigate().refresh(); // ✅ refresh to reload updated balance
    safeSleep(1000);

    openAnyAccount(ap, from, to);


    double after = ap.getBalanceFromCurrentPage();

    Assert.assertEquals(after, before - amount, 0.01, "Balance not updated correctly");
    test.pass("Balance update after transaction validated");
  }

  @Test(dataProvider = "accountTypes")
  public void viewFullTransactionHistory(String accountType) {
    loginAsJohn();
    AccountPage ap = new AccountPage(driver);
    openAnyAccount(ap, new String[] {accountType});


    ap.goToTransactionHistory();
    TransactionHistoryPage th = new TransactionHistoryPage(driver);
    Assert.assertTrue(th.isLoaded(), "Transaction history should be visible");
    Assert.assertTrue(th.getAllRowsText().size() > 0, "No transactions found");
    test.pass("Full transaction history for " + accountType + " validated");
  }

  @Test(dataProvider = "accountTypes")
  public void validateTransactionDetailsFormat(String accountType) {
    loginAsJohn();
    AccountPage ap = new AccountPage(driver);
    openAnyAccount(ap, new String[] {accountType});


    ap.goToTransactionHistory();
    Assert.assertTrue(ap.isTransactionDetailsComplete(), "Some transaction rows are missing details");

    TransactionHistoryPage th = new TransactionHistoryPage(driver);
    List<String> dates = th.getDates();
    List<String> amounts = th.getAmounts();
    List<String> types = th.getTypes();

    Assert.assertTrue(dates.stream().allMatch(d -> d.matches("\\d{2}/\\d{2}/\\d{4}")), "Invalid date format");
    Assert.assertTrue(amounts.stream().allMatch(a -> a.replaceAll("[^\\d.]", "").matches("\\d+\\.\\d{2}")), "Invalid amount format");
    Assert.assertTrue(types.stream().allMatch(t -> t.equalsIgnoreCase("Debit") || t.equalsIgnoreCase("Credit")), "Invalid transaction type");

    test.pass("Transaction details format for " + accountType + " validated");
  }

  @Test(dataProvider = "dateRanges")
  public void filterTransactionsByDateRange(String from, String to) {
    loginAsJohn();
    AccountPage ap = new AccountPage(driver);
    String opened = openAnyAccount(ap, "Checking", "Savings");
    test.info("Opened account: " + opened);




    ap.goToTransactionHistory();
    ap.filterByDate(from, to);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("transactionTable")));


    TransactionHistoryPage th = new TransactionHistoryPage(driver);
    List<String> dates = th.getDates();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    LocalDate fromDate = LocalDate.parse(from, formatter);
    LocalDate toDate = LocalDate.parse(to, formatter);

    boolean allDatesValid = dates.stream().allMatch(d -> {
      try {
        LocalDate date = LocalDate.parse(d, formatter);
        return !date.isBefore(fromDate) && !date.isAfter(toDate);
      } catch (Exception e) {
        System.out.println("Invalid date format: " + d);
        return false;
      }
    });

    Assert.assertTrue(allDatesValid, "Date filter failed");
    test.pass("Transaction filter from " + from + " to " + to + " validated");
  }

 

@Test(dataProvider = "sortFields")
  public void sortTransactionsByField(String field, String order) {
    loginAsJohn();
    AccountPage ap = new AccountPage(driver);
    openAnyAccount(ap, "Checking", "Savings");



    ap.goToTransactionHistory();
    ap.sortBy(field, order);

    TransactionHistoryPage th = new TransactionHistoryPage(driver);

    if (field.equals("Date")) {
      List<String> dates = th.getDates();
      List<String> sortedDates = dates.stream().sorted().collect(Collectors.toList());
      Assert.assertEquals(dates, sortedDates, "Date sort failed");
    } else if (field.equals("Amount")) {
      List<String> amounts = th.getAmounts();
      List<Double> numericAmounts = amounts.stream()
        .map(a -> Double.parseDouble(a.replaceAll("[^\\d.]", "")))
        .collect(Collectors.toList());
      List<Double> sortedAmounts = numericAmounts.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
      Assert.assertEquals(numericAmounts, sortedAmounts, "Amount sort failed");
    }

    test.pass(field + " sorting in " + order + " order validated");
  }
}


