package com.fintech.tests;

import com.fintech.pages.TransferPage;
import com.fintech.pages.AccountPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.List;
import org.testng.Reporter;


public class TransferTests extends BaseTest {

  private String realFrom;
  private String realTo;

  @BeforeMethod
  public void resolveAccounts() {
    loginAsJohn();
    AccountPage ap = new AccountPage(driver);
    List<String> accounts = ap.getAccountNames();
    realFrom = accounts.get(0);
    realTo = accounts.size() > 1 ? accounts.get(1) : accounts.get(0);
    System.out.println("Resolved accounts → from: " + realFrom + ", to: " + realTo);
  }

  @DataProvider(name = "fundsTransferScenarios")
  public Object[][] fundsTransferScenarios() {
    return new Object[][] {
      {"VALID_TRANSFER", "1.00", true},
      {"INSUFFICIENT_BALANCE", "99999999", false},
      {"EMPTY_FIELDS", "", false},
      {"SAME_ACCOUNT", "100.00", false},
      {"DECIMAL_AMOUNT", "0.01", true},
      {"LARGE_AMOUNT", "100000.99", true}
    };
  }

  private String describeScenario(String type) {
    return switch (type) {
      case "VALID_TRANSFER" -> "Transfer funds between valid internal accounts";
      case "INSUFFICIENT_BALANCE" -> "Transfer funds with insufficient balance";
      case "EMPTY_FIELDS" -> "Transfer funds with empty fields";
      case "SAME_ACCOUNT" -> "Attempt to transfer to the same account";
      case "DECIMAL_AMOUNT" -> "Transfer small decimal amount";
      case "LARGE_AMOUNT" -> "Transfer large or decimal amount";
      default -> "Unknown scenario";
    };
  }

  @Test(dataProvider = "fundsTransferScenarios", description = "Validates funds transfer scenarios")
  public void validateFundsTransfer(String scenarioType, String amount, boolean expectSuccess) {
    String scenario = describeScenario(scenarioType);
    test = extent.createTest(scenario);
    Reporter.log("Scenario: " + scenario, true);

    
    AccountPage ap = new AccountPage(driver);

    // ✅ Resolve actual accounts
    String from = scenarioType.equals("SAME_ACCOUNT") ? realFrom : realFrom;
    String to = scenarioType.equals("SAME_ACCOUNT") ? realFrom : realTo;

    System.out.println("Transfer → from: " + from + ", to: " + to + ", amount: " + amount);
    test.info("Transfer → from: " + from + ", to: " + to + ", amount: " + amount);

    double fromBalanceBefore = amount.isEmpty() ? 0.0 : ap.getBalance(from);
    double toBalanceBefore = amount.isEmpty() ? 0.0 : ap.getBalance(to);

    TransferPage tp = new TransferPage(driver);
    tp.selectFromByVisibleText(from);
    tp.selectToByVisibleText(to);
    tp.enterAmount(amount);
    tp.clickTransfer();

    if (amount.isEmpty()) {
      test.info("Skipping balance validation due to empty amount");
      Assert.assertTrue(tp.getError().toLowerCase().contains("amount") || !tp.isTransferConfirmed(), "Expected validation error for empty amount");
      return;
    }

    if (expectSuccess) {
      Assert.assertTrue(tp.isTransferConfirmed(), "Expected transfer to succeed");
      double parsedAmount = Double.parseDouble(amount.trim());
      double fromBalanceAfter = ap.getBalance(from);
      double toBalanceAfter = ap.getBalance(to);

      System.out.println("From balance: " + fromBalanceBefore + " → " + fromBalanceAfter);
      System.out.println("To balance: " + toBalanceBefore + " → " + toBalanceAfter);

      Assert.assertEquals(fromBalanceAfter, fromBalanceBefore - parsedAmount, 0.01, "From account balance mismatch");
      Assert.assertEquals(toBalanceAfter, toBalanceBefore + parsedAmount, 0.01, "To account balance mismatch");

    } else {
      String error = tp.getError().toLowerCase();
      test.info("Transfer error: " + error);

      switch (scenarioType) {
        case "SAME_ACCOUNT" -> Assert.assertTrue(error.contains("same account") || !tp.isTransferConfirmed(), "Expected same-account transfer to fail");
        case "INSUFFICIENT_BALANCE" -> Assert.assertTrue(error.contains("insufficient") || !tp.isTransferConfirmed(), "Expected insufficient balance error");
        case "EMPTY_FIELDS" -> Assert.assertTrue(error.contains("amount") || !tp.isTransferConfirmed(), "Expected validation error for empty fields");
        default -> Assert.assertFalse(tp.isTransferConfirmed(), "Expected transfer to fail");
      }
    }

    test.pass("Transfer scenario validated");
  }
}
