package com.fintech.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.stream.Collectors;

public class TransactionHistoryPage extends BasePage {

  private By historyHeader = By.xpath("//h1[contains(text(),'Transaction') or contains(text(),'Transactions')]");
  private By transactionRows = By.cssSelector("#transactionTable tbody tr, .transactionsTable tr");
  private By dateCell = By.cssSelector("td:nth-child(1)");
  private By amountCell = By.cssSelector("td:nth-child(2)");
  private By typeCell = By.cssSelector("td:nth-child(3)");

  public TransactionHistoryPage(WebDriver driver) {
    super(driver);
  }

  public boolean isLoaded() {
	    try {
	        wait.until(ExpectedConditions.visibilityOfElementLocated(historyHeader));
	        return true;
	    } catch (Exception e) {
	        return isDisplayed(historyHeader);
	    }
	}


  public List<String> getAllRowsText() {
    return findAll(transactionRows).stream()
      .map(WebElement::getText)
      .collect(Collectors.toList());
  }

  public List<String> getDates() {
	    wait.until(ExpectedConditions.presenceOfElementLocated(transactionRows));
	    List<WebElement> rows = findAll(transactionRows);
	    System.out.println("Transaction rows found: " + rows.size());
	    return rows.stream()
	        .map(r -> safeGet(r, dateCell))
	        .collect(Collectors.toList());
	}



  public List<String> getAmounts() {
    return findAll(transactionRows).stream()
      .map(r -> safeGet(r, amountCell))
      .collect(Collectors.toList());
  }

  public List<String> getTypes() {
    return findAll(transactionRows).stream()
      .map(r -> safeGet(r, typeCell))
      .collect(Collectors.toList());
  }

  private String safeGet(WebElement row, By cellLocator) {
    try {
      if (!row.findElements(cellLocator).isEmpty()) {
        return row.findElement(cellLocator).getText().trim();
      } else {
        return "";
      }
    } catch (Exception e) {
      return "";
    }
  }
}
