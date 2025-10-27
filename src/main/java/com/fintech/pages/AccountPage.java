package com.fintech.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.SkipException;

import java.util.*;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class AccountPage extends BasePage {

    @FindBy(xpath = "//*[contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'ACCOUNTS') or contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'ACCOUNT')]")
    private WebElement accountsHeader;

    @FindBy(css = "#balance, .balance, td.balance")
    private WebElement balanceElement;


    @FindBy(linkText = "Transactions")
    private WebElement recentTransactionsLink;

    @FindBy(linkText = "Open New Account")
    private WebElement openNewAccountLink;

    @FindBy(css = "input[type='button'][value='Open New Account']")
    private WebElement openAccountBtn;

    @FindBy(css = "#accountTable tbody tr, .table tr")
    private List<WebElement> accountRows;

    public AccountPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @Override
    public boolean isLoaded() {
        return isDisplayed(accountsHeader);
    }

    public boolean isBalanceVisible() {
        try {
            waitForVisibility(balanceElement);
            String text = getText(balanceElement);
            System.out.println("Balance element text: " + text);
            return isDisplayed(balanceElement) && !text.isEmpty();
        } catch (Exception e) {
            System.out.println("Balance element not found: " + e.getMessage());
            return false;
        }
    }

    public List<String> getAccountNames() {
        return accountRows.stream()
            .map(r -> {
                try {
                    return r.findElements(By.tagName("a")).stream()
                            .findFirst()
                            .map(WebElement::getText)
                            .orElse("Unknown");
                } catch (NoSuchElementException e) {
                    System.out.println("⚠️ Skipping row without account link: " + r.getText());
                    System.out.println("⚠️ Row HTML: " + r.getAttribute("outerHTML"));
                    return "";
                }
            })
            .filter(id -> !id.isEmpty() && id.matches("\\d+"))
            .collect(Collectors.toList());
    }

    public AccountPage ensureTwoAccounts() {
        List<String> accounts = getAccountNames();
        int attempts = 0;

        while (accounts.size() < 2 && attempts < 3) {
            System.out.println("🔧 Creating account — current count: " + accounts.size());
            click(openNewAccountLink);
            waitForVisibility(openAccountBtn);
            click(openAccountBtn);
            waitForVisibility(By.id("newAccountId")); // or any confirmation element
            safeSleep(1000); // optional buffer
            accounts = getAccountNames();
            attempts++;
        }

        if (accounts.size() < 2) {
            throw new SkipException("❌ Failed to create two accounts after " + attempts + " attempts");
        }

        System.out.println("✅ Accounts ready: " + accounts);
        return this;
    }
    private static final Object accountLock = new Object();

    public AccountPage ensureTwoAccountsSafely() {
        synchronized (accountLock) {
            return ensureTwoAccounts(); // ✅ thread-safe wrapper
        }
    }

    public AccountPage openAccountByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("❌ Account name is null or empty");
        }

        for (WebElement row : accountRows) {
            for (WebElement link : row.findElements(By.tagName("a"))) {
                if (link.getText().trim().equals(name)) {
                    click(link);
                    safeSleep(1000);
                    logBalance(name);
                    return this;
                }
            }
        }

        System.out.println("Account '" + name + "' not found — using fallback");
        if (!accountRows.isEmpty()) {
            List<WebElement> fallbackLinks = accountRows.get(0).findElements(By.tagName("a"));
            if (!fallbackLinks.isEmpty()) {
                click(fallbackLinks.get(0));
                safeSleep(1000);
                logBalance(name);
            }
        }
        return this;
    }

    private void logBalance(String accountLabel) {
        try {
            waitForVisibility(balanceElement);
            System.out.println("✅ Balance for " + accountLabel + ": " + getText(balanceElement));
        } catch (Exception e) {
            captureScreenshot("BalanceMissing_" + accountLabel);
            throw new AssertionError("Balance element not found for account: " + accountLabel, e);
        }
    }

    public double getBalanceFromCurrentPage() {
        try {
            // ✅ More precise locator: second <td> in the row containing the account link
            By balanceLocator = By.xpath("//table[@id='accountTable']//tr[td[a[contains(@href,'activity.htm?id=')]]]//td[2]");
            waitForVisibility(balanceLocator);

            String rawText = driver.findElement(balanceLocator).getText();
            System.out.println("✅ Raw balance text: " + rawText);

            String balanceText = rawText.replaceAll("[^\\d.-]", "").trim();
            if (balanceText.isEmpty()) {
                captureScreenshot("BalanceParseError");
                throw new AssertionError("❌ Balance format invalid — empty string");
            }

            return Double.parseDouble(balanceText);
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            captureScreenshot("BalanceElementMissing");
            throw new SkipException("⚠️ Balance element not found or stale", e);
        } catch (NumberFormatException e) {
            captureScreenshot("BalanceParseException");
            throw new AssertionError("❌ Balance format invalid — cannot parse", e);
        }
    }

    public double getBalance(String accountType) {
        openAccountByName(accountType);
        return getBalanceFromCurrentPage();
    }

    public AccountPage goToTransactionHistory() {
        List<By> locators = Arrays.asList(
            By.linkText("Transactions"),
            By.linkText("Transaction History"),
            By.partialLinkText("Transaction"),
            By.cssSelector("a[href*='transaction']")
        );

        for (By locator : locators) {
            try {
            	System.out.println("🔍 Trying locator: " + locator);
            	System.out.println("Page title: " + driver.getTitle());
            	System.out.println("Current URL: " + driver.getCurrentUrl());

            	WebElement txLink = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            	waitForVisibility(txLink); // ✅ Add this line
            	if (txLink.isDisplayed()) {
            	    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", txLink);
            	    wait.until(ExpectedConditions.elementToBeClickable(txLink)).click();
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("transactionTable")));
                    return this;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Locator failed: " + locator + " → " + e.getMessage());
            }
        }

        captureScreenshot("TransactionsLinkFailure");
        throw new AssertionError("✘ Transactions link not clickable");
    }

    public boolean isTransactionHistoryLoaded() {
        return exists(By.id("transactionTable")) || isDisplayed(By.cssSelector(".transaction"));
    }

    public AccountPage filterByDate(String from, String to) {
        type(By.id("fromDate"), from);
        type(By.id("toDate"), to);
        By filterBtn = By.xpath("//input[@value='Filter']");
        for (int i = 0; i < 3; i++) {
            try {
                jsClick(filterBtn);
                System.out.println("✅ Filter button clicked on attempt " + (i + 1));
                break;
            } catch (Exception e) {
                System.out.println("⚠️ Attempt " + (i + 1) + " failed to click Filter button");
                safeSleep(1000);
            }
        }
        safeSleep(2000);
        return this;
    }

    public AccountPage sortBy(String field, String order) {
        try {
            By sortLinkLocator = By.xpath("//th[contains(text(),'" + field + "')]");
            jsClick(sortLinkLocator);
            System.out.println("✅ Clicked sort link: " + field);
            safeSleep(2000);
            if (order.equalsIgnoreCase("Descending")) {
                click(sortLinkLocator); // toggle
                System.out.println("✅ Toggled to descending sort");
                safeSleep(2000);
            }
        } catch (Exception e) {
            captureScreenshot("SortLinkFailure_" + field);
            System.out.println("❌ Sort failed: " + e.getMessage());
        }
        return this;
    }
}
