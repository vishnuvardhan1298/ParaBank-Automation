package com.fintech.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class AccountPage extends BasePage {

  private WebDriver driver;
  private WebDriverWait wait;

  private By accountsHeader = By.xpath("//*[contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'ACCOUNTS') or contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'ACCOUNT')]");
  private By accountRows = By.cssSelector("#accountTable tbody tr, .table tr");
  private By balanceSelector = By.xpath("//*[contains(text(),'Balance') or contains(@class,'balance')]");
  private By recentTransactionsLink = By.linkText("Transactions");

  public AccountPage(WebDriver driver) {
    super(driver);
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
  }

  public boolean isLoaded() {
    System.out.println("AccountPage check → URL: " + driver.getCurrentUrl());
    System.out.println("AccountPage check → Title: " + driver.getTitle());

    try {
      By accountTable = By.id("accountTable");
      wait.until(ExpectedConditions.or(
        ExpectedConditions.visibilityOfElementLocated(accountTable),
        ExpectedConditions.visibilityOfElementLocated(accountsHeader),
        ExpectedConditions.visibilityOfElementLocated(By.linkText("Log Out"))
      ));
      return true;
    } catch (Exception e) {
      return isDisplayed(accountsHeader) || isDisplayed(By.id("accountTable")) || isDisplayed(By.linkText("Log Out"));
    }
  }

  public List<String> getAccountNames() {
    List<WebElement> rows = findAll(accountRows);
    return rows.stream()
      .map(r -> r.getText().split("\\r?\\n")[0].trim())
      .filter(s -> !s.isEmpty())
      .collect(Collectors.toList());
  }

  public void openAccountByName(String name) {
    wait.until(ExpectedConditions.or(
      ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a[href*='activity.htm']")),
      ExpectedConditions.presenceOfElementLocated(By.id("accountTable"))
      
    ));

    System.out.println("Account links or table detected — proceeding to open account: " + name);

    List<WebElement> rows = findAll(accountRows);
    System.out.println("Account names found: " + getAccountNames());
    System.out.println("Available accounts:");
    for (WebElement r : rows) {
      System.out.println("→ " + r.getText());
    }

    for (WebElement r : rows) {
      if (r.getText().toLowerCase().contains(name.toLowerCase())) {
        List<WebElement> links = r.findElements(By.tagName("a"));
        if (!links.isEmpty()) {
          System.out.println("Opening account: " + name);
          links.get(0).click();
          safeSleep(1000);
          return;
        }
      }
    }

    if (!rows.isEmpty()) {
      List<WebElement> fallbackLinks = rows.get(0).findElements(By.tagName("a"));
      if (!fallbackLinks.isEmpty()) {
        String fallbackName = rows.get(0).getText().split("\\r?\\n")[0].trim();
        System.out.println("Fallback row → " + rows.get(0).getText());
        System.out.println("Fallback click → " + fallbackLinks.get(0).getText());
        System.out.println("Account '" + name + "' not found — using fallback: " + fallbackName);
        fallbackLinks.get(0).click();
        safeSleep(1000);
        return;
      }
    }

    throw new AssertionError("No account links found — cannot proceed");
  }

  public boolean isBalanceVisible() {
    return exists(balanceSelector) && isDisplayed(balanceSelector);
  }

  public void goToTransactionHistory() {
    WebElement txLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transactions")));
    txLink.click();
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("transactionTable")));
    System.out.println("Navigated to transaction history — table loaded");
  }

  public double getBalance(String accountType) {
	  try {
	    openAccountByName(accountType);

	    wait.until(ExpectedConditions.or(
	      ExpectedConditions.visibilityOfElementLocated(balanceSelector),
	      ExpectedConditions.presenceOfElementLocated(balanceSelector)
	    ));

	    WebElement balanceElement = driver.findElement(balanceSelector);
	    String balanceText = balanceElement.getText().replaceAll("[^\\d.-]", "");

	    if (balanceText.isEmpty()) {
	      System.out.println("Balance text is empty for account: " + accountType);
	      throw new AssertionError("Balance not found or not visible for account: " + accountType);
	    }

	    double balance = Double.parseDouble(balanceText);
	    System.out.println("Balance for account '" + accountType + "': " + balance);
	    return balance;

	  } catch (TimeoutException te) {
	    System.out.println("Timeout while waiting for balance element for account: " + accountType);
	    throw new AssertionError("Balance element not found in time for account: " + accountType, te);
	  } catch (NoSuchElementException ne) {
	    System.out.println("Balance element missing for account: " + accountType);
	    throw new AssertionError("Balance element not present for account: " + accountType, ne);
	  } catch (NumberFormatException nfe) {
	    System.out.println("Unable to parse balance for account: " + accountType);
	    throw new AssertionError("Balance format invalid for account: " + accountType, nfe);
	  } catch (Exception e) {
	    System.out.println("Unexpected error while fetching balance for account: " + accountType);
	    throw new AssertionError("Unexpected error in getBalance()", e);
	  }
	}


  public double getBalanceFromCurrentPage() {
    WebElement balanceElement = driver.findElement(balanceSelector);
    String balanceText = balanceElement.getText().replaceAll("[^\\d.]", "");
    return Double.parseDouble(balanceText);
  }

  public void transferFunds(String from, String to, double amount) {
    click(By.linkText("Transfer Funds"));
    selectDropdown(By.id("fromAccountId"), from);
    selectDropdown(By.id("toAccountId"), to);
    type(By.id("amount"), String.valueOf(amount));
    click(By.xpath("//input[@value='Transfer']"));
  }

  public boolean isTransactionListVisible() {
    return isDisplayed(By.id("transactionTable")) || isDisplayed(By.cssSelector(".transaction"));
  }

  public boolean validateTransactionFormat() {
    List<WebElement> rows = driver.findElements(By.cssSelector("#transactionTable tbody tr"));
    for (WebElement row : rows) {
      String text = row.getText();
      if (!(text.contains("Debit") || text.contains("Credit"))) return false;
      if (!text.matches(".*\\d{2}/\\d{2}/\\d{4}.*")) return false;
      if (!text.matches(".*\\d+\\.\\d{2}.*")) return false;
    }
    return true;
  }

  public void filterByDate(String from, String to) {
    type(By.id("fromDate"), from);
    type(By.id("toDate"), to);
    click(By.xpath("//input[@value='Filter']"));
  }

  public boolean areFilteredDatesCorrect(String from, String to) {
    List<WebElement> rows = driver.findElements(By.cssSelector("#transactionTable tbody tr"));
    LocalDate fromDate = LocalDate.parse(from);
    LocalDate toDate = LocalDate.parse(to);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    for (WebElement row : rows) {
      String dateText = row.findElement(By.cssSelector("td.date")).getText();
      LocalDate date = LocalDate.parse(dateText, formatter);
      if (date.isBefore(fromDate) || date.isAfter(toDate)) return false;
    }
    return true;
  }

  public void sortBy(String field, String order) {
    By sortLink = By.linkText(field);
    click(sortLink);
    if (order.equalsIgnoreCase("Descending")) click(sortLink); // toggle
  }

  public boolean isSortedByDateAsc() {
    List<LocalDate> dates = driver.findElements(By.cssSelector("td.date")).stream()
      .map(e -> LocalDate.parse(e.getText(), DateTimeFormatter.ofPattern("MM/dd/yyyy")))
      .collect(Collectors.toList());
    return dates.equals(dates.stream().sorted().collect(Collectors.toList()));
  }

  public boolean isSortedByAmountDesc() {
    List<Double> amounts = driver.findElements(By.cssSelector("td.amount")).stream()
      .map(e -> Double.parseDouble(e.getText().replaceAll("[^\\d.]", "")))
      .collect(Collectors.toList());
    return amounts.equals(amounts.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
  }

  public void clickSortLink(String linkText) {
    wait.until(ExpectedConditions.elementToBeClickable(By.linkText(linkText)));
    click(By.linkText(linkText));
  }

  public boolean isTransactionDetailsComplete() {
    List<WebElement> rows = driver.findElements(By.cssSelector("#transactionTable tbody tr"));
    for (WebElement row : rows) {
      String date = row.findElement(By.cssSelector("td.date")).getText();
      String amount = row.findElement(By.cssSelector("td.amount")).getText();
      String type = row.findElement(By.cssSelector("td.type")).getText();

      if (date.isEmpty() || amount.isEmpty() || type.isEmpty()) {
        System.out.println("Incomplete row → date: " + date + ", amount: " + amount + ", type: " + type);
        return false;
      }
    }
    return true;
  }

  // Utility methods from BasePage assumed:
  // - findAll(By)
  // - exists(By)
  // - isDisplayed(By)
  // - click(By)
  // - type(By, String)
  // - selectDropdown(By, String)
}
