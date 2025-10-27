package com.fintech.tests;

import com.fintech.pages.AccountPage;
import com.fintech.pages.TransferPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.fintech.assertions.AssertUtils;
import com.fintech.dataproviders.JsonDataProvider;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class TransferTests extends BaseTest {

    private AccountPage accountPage;
    private TransferPage transferPage;
    private String fromAccount;
    private String toAccount;

    @BeforeClass(alwaysRun = true)
    public void setupAccountsOnce() {
        loginAsJohn();
        accountPage = new AccountPage(driver);
        accountPage.ensureTwoAccountsSafely(); // ✅ Thread-safe creation
    }

    @BeforeMethod
    public void setupTransferContext() {
        if (accountPage == null || !accountPage.isSessionActive()) {
            test = extent.createTest("Transfer Setup → Skipped due to session loss");
            test.skip("Session ended — skipping test");
            throw new SkipException("Session ended");
        }

        transferPage = new TransferPage(driver);

        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.or(
                ExpectedConditions.titleContains("Accounts Overview"),
                ExpectedConditions.titleContains("Transfer Funds"),
                ExpectedConditions.titleContains("Open Account")
            ));

        List<String> accountIds = accountPage.getAccountNames().stream()
            .filter(id -> id.matches("\\d+"))
            .limit(10)
            .collect(Collectors.toList());

        List<String> usableAccounts = accountIds.stream()
            .filter(acc -> {
                try {
                    double balance = accountPage.getBalance(acc);
                    return balance >= 1.00;
                } catch (SkipException se) {
                    System.out.println("⚠️ Skipping unusable account: " + acc);
                    return false;
                }
            })
            .collect(Collectors.toList());

        if (usableAccounts.size() < 2) {
            captureScreenshot("TransferSetupFailed");
            test = extent.createTest("Transfer Setup → Skipped due to account selection failure");
            test.skip("❌ Skipping test — not enough usable accounts");
            throw new SkipException("❌ Skipping test — account selection failed");
        }

        fromAccount = usableAccounts.get(0);
        toAccount = usableAccounts.get(1);

        System.out.println("✅ Setup complete → From: " + fromAccount + " | To: " + toAccount);
    }


    @Test(dataProvider = "transferDataJson", dataProviderClass = JsonDataProvider.class)
    public void validateFundsTransfer(String scenarioLabel, String amount, boolean expectSuccess) {
        test = extent.createTest("Transfer Scenario → " + scenarioLabel + " | Amount: " + amount);

        if (scenarioLabel.equalsIgnoreCase("EMPTY_FIELDS")) {
            transferPage.transferFunds("", "", 0.0);
            test.info("Submitted transfer with empty fields");
            String error = transferPage.getError();
            Assert.assertTrue(error.length() > 0, "Expected validation error");
            test.pass("Validation error displayed for empty fields");
            return;
        }

        if (scenarioLabel.equalsIgnoreCase("SAME_ACCOUNT")) {
            transferPage.transferFunds(fromAccount, fromAccount, Double.parseDouble(amount));
            test.info("Attempted transfer to same account");
            String error = transferPage.getError();
            Assert.assertTrue(error.length() > 0, "Expected error for same account transfer");
            test.pass("Error displayed for same account transfer");
            return;
        }

        double transferAmount = Double.parseDouble(amount);
        double fromBalanceBefore = accountPage.getBalance(fromAccount);
        double toBalanceBefore = accountPage.getBalance(toAccount);

        test.info("From Balance Before: " + fromBalanceBefore);
        test.info("To Balance Before: " + toBalanceBefore);

        transferPage.transferFunds(fromAccount, toAccount, transferAmount);
        test.info("Transfer submitted → " + transferAmount);

        if (expectSuccess) {
            Assert.assertTrue(transferPage.isTransferConfirmed(), "❌ Transfer was not confirmed");
            test.pass("Transfer confirmed via UI — skipping backend validation");
        } else {
            String error = transferPage.getError();
            Assert.assertFalse(transferPage.isTransferConfirmed(), "❌ Transfer should have failed but was confirmed");
            Assert.assertTrue(error.length() > 0, "❌ Expected error message not shown");
            test.pass("Transfer rejected as expected");
        }
    }

    @Test(retryAnalyzer = com.fintech.utils.RetryAnalyzer.class)
    public void validateSimpleTransferSuccess() {
        transferPage.transferFunds("12345", "12456", 1.00);
        String confirmation = transferPage.getConfirmationText();
        AssertUtils.assertTransferSuccess(confirmation);
    }
}

