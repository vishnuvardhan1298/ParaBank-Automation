package com.fintech.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionHistoryPage extends BasePage {

    @FindBy(xpath = "//h1[contains(text(),'Transaction') or contains(text(),'Transactions')]")
    private WebElement historyHeader;

    @FindBy(css = "table[id*='transaction'] tbody, .transactionsTable")
    private WebElement transactionTable;

    @FindBy(id = "accountId")
    private WebElement accountDropdown;

    @FindBy(id = "findById")
    private WebElement findButton;

    public TransactionHistoryPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @Override
    public boolean isLoaded() {
        return isTransactionHistoryLoaded();
    }

    public TransactionHistoryPage selectAccount(String accountId) {
        try {
            selectDropdown(accountDropdown, accountId);
            System.out.println("✅ Selected account: " + accountId);
        } catch (Exception e) {
            captureScreenshot("AccountDropdownError_" + accountId);
            throw new AssertionError("✘ Could not select account: " + accountId);
        }
        return this;
    }

    public TransactionHistoryPage clickFindTransactions() {
        try {
            click(findButton);
            waitForVisibility(transactionTable);
            System.out.println("✅ Clicked 'Find Transactions' button");
        } catch (Exception e) {
            captureScreenshot("FindTransactionsClickError");
            throw new AssertionError("✘ Could not click 'Find Transactions'");
        }
        return this;
    }

    public boolean isTransactionHistoryLoaded() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("transactionTable")));
            int rowCount = driver.findElements(By.cssSelector("#transactionBody tr")).size();
            
            if (rowCount == 0) {
                safeSleep(2000);
                rowCount = driver.findElements(By.cssSelector("#transactionBody tr")).size();
            }
            
            System.out.println("🔍 Transaction row count: " + rowCount);
            return rowCount > 0;
        } catch (Exception e) {
            System.out.println("⚠️ Transaction table not found: " + e.getMessage());
            captureScreenshot("TransactionTableNotFound");
            return false;
        }
    }

    public List<String> getAllRowsText() {
        List<WebElement> rows = transactionTable.findElements(By.tagName("tr"));
        if (rows.isEmpty()) {
            safeSleep(2000);
            rows = transactionTable.findElements(By.tagName("tr"));
            if (rows.isEmpty()) {
                captureScreenshot("TransactionRowsMissing");
                return Collections.emptyList();
            }
        }
        return rows.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public List<String> debugRawTransactionRows() {
        List<WebElement> rows = transactionTable.findElements(By.tagName("tr"));
        List<String> texts = new ArrayList<>();
        for (WebElement row : rows) {
            String text = row.getText();
            System.out.println("→ Row text: " + text);
            texts.add(text);
        }
        return texts;
    }

    public List<String> getDates() {
        return extractColumnText(1);
    }

    public List<String> getAmounts() {
        return extractColumnText(2);
    }

    public List<String> getTypes() {
        return extractColumnText(3);
    }

    private List<String> extractColumnText(int columnIndex) {
        List<WebElement> rows = transactionTable.findElements(By.tagName("tr"));
        if (rows.isEmpty()) {
            safeSleep(2000);
            rows = transactionTable.findElements(By.tagName("tr"));
            if (rows.isEmpty()) {
                captureScreenshot("TransactionRowsMissing");
                return Collections.emptyList();
            }
        }
        return rows.stream().map(row -> safeGet(row, columnIndex)).collect(Collectors.toList());
    }

    private String safeGet(WebElement row, int columnIndex) {
        try {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() >= columnIndex) {
                return cells.get(columnIndex - 1).getText().trim();
            }
        } catch (Exception ignored) {}
        return "";
    }

    public void logTableHeaders() {
        List<WebElement> headers = driver.findElements(By.cssSelector("table[id*='transaction'] th"));
        System.out.println("🔍 Table headers:");
        headers.forEach(h -> System.out.println("→ " + h.getText()));
    }
}

