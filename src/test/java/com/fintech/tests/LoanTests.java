package com.fintech.tests;

import com.fintech.pages.AccountPage;
import com.fintech.pages.LoanPage;
import com.fintech.utils.TestDataProvider;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class LoanTests extends BaseTest {

    @Test(priority = 3, dataProvider = "loanDataExcel", dataProviderClass = TestDataProvider.class)
    public void applyLoan(String scenarioLabel, Double amount, Double downPayment, Double accountId, Boolean expectSuccess) {
        String amountStr = amount == null ? "" : String.valueOf(amount.intValue());
        String downPaymentStr = downPayment == null ? "" : String.valueOf(downPayment.intValue());
        String accountIdStr = accountId == null ? "" : String.valueOf(accountId.intValue());

        test = extent.createTest("Loan Scenario → " + scenarioLabel);
        LoanPage loanPage = new LoanPage(driver);
        if (!loanPage.isSessionActive()) {
            test.skip("Session ended — skipping test");
            throw new SkipException("Session ended");
        }

        test.info("Running scenario: " + scenarioLabel);
        test.info("Amount: " + amountStr + ", DownPayment: " + downPaymentStr + ", AccountId: " + accountIdStr + ", ExpectSuccess: " + expectSuccess);

        loginAsJohn();
        AccountPage ap = new AccountPage(driver);
        ap.ensureTwoAccounts();

        LoanPage loan = new LoanPage(driver);
        loan.open();

        if (!loan.isLoanFormLoaded()) {
            captureScreenshot("LoanFormNotLoaded_" + scenarioLabel.replace(" ", "_"));
            Assert.fail("❌ Loan form not loaded — dropdown or fields missing");
        }

        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.urlContains("requestloan"));
        Assert.assertTrue(driver.getCurrentUrl().contains("requestloan"), "❌ Unexpected redirect — not on loan form");

        loan.logAllAccountBalances();
        String actualAccountId = "";

        if (!amountStr.isEmpty() && !downPaymentStr.isEmpty()) {
            safeSleep(1000);          

            List<String> validAccounts = loan.getAllAccountIds().stream()
                .filter(acc -> {
                    String bal = loan.getBalanceForAccount(acc);
                    return !bal.startsWith("-") && !bal.equals("$0.00");
                })
                .collect(Collectors.toList());
            String bal12789 = loan.getBalanceForAccount("12789");
            if (bal12789 == null || bal12789.trim().isEmpty()) {
                test.warning("⚠️ Balance for 12789 could not be retrieved or is empty.");
            } else {
                test.info("Balance for 12789: " + bal12789);
            }

            test.info("Valid accounts with usable balance: " + String.join(", ", validAccounts));


            if (validAccounts.isEmpty()) {
                test.skip("❌ Skipping test — no account with positive balance available");
                throw new SkipException("❌ Skipping test — no account with positive balance");
            }
            if (!validAccounts.contains(accountIdStr)) {
                String bal = loan.getBalanceForAccount(accountIdStr);
                test.warning("⚠️ Account " + accountIdStr + " not in valid list. Balance: " + bal);
            }

            actualAccountId = (accountIdStr.isEmpty() || !validAccounts.contains(accountIdStr))
                ? validAccounts.get(0)
                : accountIdStr;
            test.info("Using account: " + actualAccountId);
            String usedBalance = loan.getBalanceForAccount(actualAccountId);
            test.info("Balance for selected account " + actualAccountId + ": " + usedBalance);


          
            try {
                Select dropdown = new Select(driver.findElement(By.id("fromAccountId")));
                dropdown.selectByVisibleText(actualAccountId);
                Assert.assertEquals(dropdown.getFirstSelectedOption().getText(), actualAccountId, "❌ Dropdown selection failed");
            } catch (Exception e) {
                captureScreenshot("loan_form_missing_" + scenarioLabel.replace(" ", "_"));
                Assert.fail("❌ Loan form element missing: " + e.getMessage());
            }
        }

        if (amountStr.isEmpty() || downPaymentStr.isEmpty()) {
            driver.get("https://parabank.parasoft.com/parabank/requestloan.htm");
            safeSleep(1000);
            try {
                new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error, .errorMessage, #loanStatus")));
                List<WebElement> errors = driver.findElements(By.cssSelector(".error, .errorMessage, #loanStatus"));
                boolean found = errors.stream().anyMatch(e -> e.isDisplayed() && !e.getText().trim().isEmpty());
                Assert.assertTrue(found, "Expected validation error");
                test.pass("Validation error displayed for missing input");
            } catch (TimeoutException e) {
                captureScreenshot("MissingInfoValidationFailed");
                Assert.fail("❌ Validation error not found for missing input");
            }
            return;
        }

        loan.enterAmount(amountStr);
        loan.enterDownPayment(downPaymentStr);
        loan.clickApply();
        safeSleep(2000);

        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.visibilityOfElementLocated(By.id("loanStatus")));
        } catch (TimeoutException e) {
            captureScreenshot("loan_status_timeout_" + amountStr);
            Assert.fail("Loan status not visible — possible form failure or locator issue.");
        }

        String result = loan.getResult().trim().toLowerCase();
        test.info("Loan result: " + result);

        if (result.isEmpty()) {
            captureScreenshot("loan_result_missing_" + amountStr);
            Assert.fail("Loan result not found after submission.");
        }
        test.info("Expected approval: " + expectSuccess);
        test.info("Actual approval: " + loan.isLoanApproved());


        try {
            if (expectSuccess) {
                Assert.assertTrue(result.contains("approved") || result.contains("congratulations"), "Expected approval");
            } else {
                Assert.assertTrue(result.contains("denied") || result.contains("error") || result.contains("rejected"), "Expected rejection");
            }
            test.pass("Matched keyword: " + result);
            captureScreenshot("LoanApproved_" + scenarioLabel.replace(" ", "_") + "_" + amountStr);
        } catch (AssertionError e) {
            test.fail("Assertion failed for scenario: " + scenarioLabel);
            test.fail("Matched keyword not found in result: " + result);
            captureScreenshot("loan_assertion_failure_" + scenarioLabel.replace(" ", "_") + "_" + amountStr);
            throw e;
        }        

        if (scenarioLabel.equalsIgnoreCase("Status Verification")) {
            driver.get("https://parabank.parasoft.com/parabank/loanstatus.htm");
            try {
                new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.visibilityOfElementLocated(By.id("loanStatus")));
                WebElement status = driver.findElement(By.id("loanStatus"));
                Assert.assertTrue(status.getText().toLowerCase().contains("approved") || status.getText().toLowerCase().contains("denied"), "Expected loan status to be visible");
                test.pass("Loan status page confirmed: " + status.getText());
            } catch (TimeoutException e) {
                captureScreenshot("loan_status_page_timeout_" + amountStr);
                Assert.fail("Loan status page did not load correctly.");
            }
        }

        test.pass("Loan scenario validated");
    }

    @Test(priority = 2)
    public void submitValidLoanRequest() {
        test = extent.createTest("Submit Valid Loan Request");
        LoanPage loanPage = new LoanPage(driver);
        if (!loanPage.isSessionActive()) {
            test.skip("Session ended — skipping test");
            throw new SkipException("Session ended");
        }

        loginAsJohn();
        AccountPage ap = new AccountPage(driver);
        ap.ensureTwoAccounts();

        loanPage.open();

        // 🔧 Fallback retry if loan form doesn't load
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
        } catch (TimeoutException e) {
            System.out.println("⚠️ First attempt failed — retrying loan form load");
            driver.navigate().refresh();
            safeSleep(1000);
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
        }

        String accountId = loanPage.getFirstAccountId();
        loanPage.selectFromAccount(accountId);

        String balance = loanPage.getBalanceForAccount(accountId);
        test.info("Selected account balance: " + balance);
        if (balance.startsWith("-") || balance.equals("$0.00")) {
            List<String> validAccounts = loanPage.getAllAccountIds().stream()
                .filter(acc -> {
                    String bal = loanPage.getBalanceForAccount(acc);
                    return !bal.startsWith("-") && !bal.equals("$0.00");
                })
                .collect(Collectors.toList());

            if (validAccounts.isEmpty()) {
                captureScreenshot("NoValidAccounts");
                Assert.fail("❌ No account with sufficient balance found");
            }

            accountId = validAccounts.get(0);
            loanPage.selectFromAccount(accountId);
            balance = loanPage.getBalanceForAccount(accountId);
            test.info("Fallback account selected: " + accountId + " → Balance: " + balance);
        }

        loanPage.enterAmount("5000");
        loanPage.enterDownPayment("500");
        loanPage.clickApply();

        String rawResult = loanPage.getResult();
        test.info("Raw loan result: " + rawResult);
        String result = rawResult.trim().toLowerCase();
        test.info("Loan result: " + result);
        Assert.assertTrue(result.contains("approved") || result.contains("congratulations"), "❌ Loan not approved as expected");
        test.pass("Loan approved successfully");
        captureScreenshot("LoanApproved_" + accountId);
    }


    @Test(priority = 1)
    public void testLoanTyping() {
        test = extent.createTest("Test Loan Typing");
        LoanPage loanPage = new LoanPage(driver);
        if (!loanPage.isSessionActive()) {
            test.skip("Session ended — skipping test");
            throw new SkipException("Session ended");
        }

        loginAsJohn();
        AccountPage ap = new AccountPage(driver);
        ap.ensureTwoAccounts();

        LoanPage loan = new LoanPage(driver);
        loan.open();
        loan.enterAmount("5000");
        loan.enterDownPayment("500");
        loan.logout(); // ensures clean state
        test.pass("Loan typing test completed");
    }
}

