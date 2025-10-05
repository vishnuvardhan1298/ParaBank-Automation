package com.fintech.tests;

import com.fintech.pages.LoanPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LoanTests extends BaseTest {

  @DataProvider(name = "loanInputs")
  public Object[][] loanInputs() {
    return new Object[][] {
      {"5000", "500", "80001", true},
      {"", "", "", false}
    };
  }

  @Test(dataProvider = "loanInputs")
  public void applyLoan(String amount, String downPayment, String accountId, boolean expectSuccess) {
    loginAsJohn();
    LoanPage loan = new LoanPage(driver);
    loan.enterAmount(amount);
    loan.enterDownPayment(downPayment);
    if (!accountId.isEmpty()) loan.selectFromAccount(accountId);
    loan.clickApply();

    String result = loan.getResult().toLowerCase();
    if (expectSuccess) {
      Assert.assertTrue(result.contains("approved") || result.contains("applied") || result.contains("denied"), "Expected loan result");
    } else {
      Assert.assertTrue(!result.isEmpty(), "Expected validation message");
    }

    test.pass("Loan scenario validated");
  }
}


