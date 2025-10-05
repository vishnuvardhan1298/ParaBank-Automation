package com.fintech.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

public class TransferPage extends BasePage {
  private By fromAccount = By.id("fromAccountId");
  private By toAccount = By.id("toAccountId");
  private By amount = By.id("amount");
  private By transferBtn = By.xpath("//input[@value='Transfer' or @type='submit']");
  private By confirmation = By.xpath("//h1|//div[contains(@class,'result') or contains(@class,'title')]");
  private By errorMsg = By.cssSelector("#rightPanel .error");

  public TransferPage(WebDriver driver) { super(driver); }

  public void selectFromByVisibleText(String text) {
    try { new Select(find(fromAccount)).selectByVisibleText(text); } catch (Exception ignored) {}
  }

  public void selectToByVisibleText(String text) {
    try { new Select(find(toAccount)).selectByVisibleText(text); } catch (Exception ignored) {}
  }

  public void enterAmount(String amt) { type(amount, amt); }

  public void clickTransfer() { click(transferBtn); }

  public boolean isTransferConfirmed() {
	  return isDisplayed(By.xpath("//*[contains(text(),'Transfer Complete') or contains(text(),'successfully transferred')]"));
	}

  //src/main/java/com/fintech/pages/TransferPage.java
  public boolean isLoaded() {
  return exists(fromAccount) || isDisplayed(amount) || isDisplayed(transferBtn);
  }


  public String getError() { return getText(errorMsg); }
}


