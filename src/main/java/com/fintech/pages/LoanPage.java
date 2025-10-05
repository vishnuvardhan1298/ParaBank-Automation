package com.fintech.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

public class LoanPage extends BasePage {
  private By amount = By.id("amount");
  private By downPayment = By.id("downPayment");
  private By fromAccount = By.id("fromAccountId");
  private By applyBtn = By.xpath("//input[@value='Apply Now' or @type='submit']");
  private By resultMsg = By.cssSelector("#rightPanel .title, #rightPanel .result");
  private By errorMsg = By.cssSelector("#rightPanel .error");

  public LoanPage(WebDriver driver) { super(driver); }

  public void enterAmount(String amt) { type(amount, amt); }

  public void enterDownPayment(String dp) { type(downPayment, dp); }

  public void selectFromAccount(String visibleText) {
    try { new Select(find(fromAccount)).selectByVisibleText(visibleText); } catch (Exception ignored) {}
  }

  public void clickApply() { click(applyBtn); }

  public String getResult() {
    String r = getText(resultMsg);
    if (r.isEmpty()) r = getText(errorMsg);
    return r;
  }

  public boolean isSubmitted() {
    String lower = getResult().toLowerCase();
    return lower.contains("approved") || lower.contains("denied") || lower.contains("applied");
  }
  public boolean isLoaded() {
	  return isDisplayed(applyBtn) || exists(applyBtn);
  }
  public void logout() {
	  try {
	    click(By.linkText("Log Out"));
	  } catch (Exception e) {
	    // fallback to direct driver if needed
	    driver.findElement(By.linkText("Log Out")).click();
	  }
	}

}


