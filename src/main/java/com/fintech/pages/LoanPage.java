package com.fintech.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.stream.Collectors;

public class LoanPage extends BasePage {

    @FindBy(id = "amount")
    private WebElement amountField;

    @FindBy(id = "downPayment")
    private WebElement downPaymentField;

    @FindBy(id = "fromAccountId")
    private WebElement fromAccountDropdown;

    @FindBy(xpath = "//input[@value='Apply Now' or @type='submit']")
    private WebElement applyButton;

    @FindBy(css = "#rightPanel .title, #rightPanel .result")
    private WebElement resultMessage;

    @FindBy(css = "#rightPanel .error")
    private WebElement errorMessage;

    @FindBy(linkText = "Apply for a Loan")
    private WebElement applyLoanLink;

    @FindBy(id = "loanStatus")
    private WebElement loanStatus;

    @FindBy(css = "#loanRequestApproved p")
    private WebElement approvalMessage;

    public LoanPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @Override
    public boolean isLoaded() {
        return isLoanFormLoaded();
    }

    public LoanPage open() {
        navigateTo("https://parabank.parasoft.com/parabank/requestloan.htm");

        for (int i = 0; i < 3; i++) {
            if (isDisplayed(fromAccountDropdown)) break;
            System.out.println("⚠️ Retry " + (i + 1) + ": Loan form not ready, refreshing...");
            driver.navigate().refresh();
            safeSleep(1000);
        }

        if (!isDisplayed(fromAccountDropdown)) {
            captureScreenshot("LoanDropdownStillMissing");
            throw new AssertionError("❌ Loan dropdown still missing after retries");
        }

        System.out.println("✅ Loan form loaded successfully.");
        return this;
    }

    public LoanPage enterAmount(String amount) {
        type(amountField, amount);
        return this;
    }

    public LoanPage enterDownPayment(String payment) {
        type(downPaymentField, payment);
        return this;
    }

    public LoanPage selectFromAccount(String accountId) {
        selectDropdown(fromAccountDropdown, accountId);
        return this;
    }

    public LoanPage clickApply() {
        click(applyButton);
        return this;
    }

    public boolean isLoanFormLoaded() {
        return isDisplayed(amountField) && isDisplayed(downPaymentField) && isDisplayed(fromAccountDropdown);
    }

    public boolean isLoanApproved() {
        String resultText = getResult().toLowerCase();
        System.out.println("🔍 Loan result text: " + resultText);
        boolean approved = resultText.contains("approved") || resultText.contains("congratulations");
        if (!approved) captureScreenshot("LoanApprovalMissing");
        return approved;
    }

    public boolean isSubmitted() {
        String lower = getResult().toLowerCase();
        return lower.contains("approved") || lower.contains("denied") || lower.contains("rejected") || lower.contains("submitted");
    }

    public String getResult() {
        for (int i = 0; i < 3; i++) {
            try {
                String statusText = getText(loanStatus);
                String approvalText = getText(approvalMessage);
                String combined = statusText + " " + approvalText;
                if (!combined.isEmpty()) {
                    System.out.println("✅ Loan result fetched: " + combined);
                    return combined.trim();
                }
            } catch (Exception e) {
                System.out.println("⚠️ getResult() attempt " + (i + 1) + " failed: " + e.getMessage());
            }
            safeSleep(1000);
        }
        captureScreenshot("LoanResultMissing");
        return "";
    }

    public String getLoanError() {
        return getText(errorMessage);
    }

    public String getFirstAccountId() {
        try {
            Select dropdown = new Select(fromAccountDropdown);
            return dropdown.getOptions().get(0).getText();
        } catch (Exception e) {
            System.out.println("⚠️ Account dropdown not found.");
            return "";
        }
    }

    public List<String> getAllAccountIds() {
        for (int i = 0; i < 3; i++) {
            try {
                WebElement dropdown = driver.findElement(By.id("fromAccountId"));
                Select select = new Select(dropdown);
                return select.getOptions().stream()
                    .map(WebElement::getText)
                    .filter(text -> text.matches("\\d+"))
                    .collect(Collectors.toList());
            } catch (Exception e) {
                System.out.println("⚠️ Retry " + (i + 1) + ": fromAccountId not found, refreshing...");
                driver.navigate().refresh();
                safeSleep(1000);
            }
        }
        captureScreenshot("LoanDropdownMissing");
        throw new AssertionError("❌ Loan form dropdown not found after retries");
    }

    public String getBalanceForAccount(String accountId) {
        try {
            navigateTo("https://parabank.parasoft.com/parabank/overview.htm");
            WebElement balanceCell = driver.findElement(
                By.xpath("//a[text()='" + accountId + "']/ancestor::td/following-sibling::td")
            );
            String balance = balanceCell.getText().trim();
            if (balance.isEmpty()) {
                captureScreenshot("EmptyBalance_" + accountId);
                System.out.println("⚠️ Balance cell is empty for account: " + accountId);
            }
            return balance;
        } catch (Exception e) {
            System.out.println("⚠️ Failed to get balance for account " + accountId + ": " + e.getMessage());
            return "";
        }
    }

    public void logAllAccountBalances() {
        navigateTo("https://parabank.parasoft.com/parabank/overview.htm");
        List<WebElement> rows = driver.findElements(By.xpath("//table[contains(@id,'accountTable')]//tr[td]"));
        for (WebElement row : rows) {
            if (!row.getText().matches(".*\\d{4,}.*")) continue;
            try {
                String accountId = row.findElement(By.xpath("./td[1]/a")).getText().trim();
                String balance = row.findElement(By.xpath("./td[2]")).getText().trim();
                System.out.println("Account: " + accountId + " → Balance: " + balance);
            } catch (Exception e) {
                System.out.println("Skipping malformed row: " + row.getText());
            }
        }
    }

    public void logout() {
        try {
            click(By.linkText("Log Out"));
        } catch (Exception e) {
            try {
                driver.findElement(By.linkText("Log Out")).click();
            } catch (Exception ignored) {}
        }
    }
}
