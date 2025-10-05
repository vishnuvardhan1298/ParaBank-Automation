package com.fintech.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class NavigationPage extends BasePage {

  private By homeLink = By.xpath("//a[contains(@href,'index.htm') or contains(@class,'brand') or contains(@class,'logo')]");
  private By accountsLink = By.linkText("Accounts Overview");
  private By transferLink = By.linkText("Transfer Funds");
  private By loanLink = By.linkText("Request Loan");
  private By contactLink = By.linkText("Contact Us");
  private By logo = By.cssSelector("a.brand, a.logo");

  public NavigationPage(WebDriver driver) {
    super(driver);
  }

  public void goHome() {
    click(homeLink);
  }

  public void goToAccounts() {
    click(accountsLink);
  }

  public void goToTransfer() {
    click(transferLink);
  }

  public void goToLoan() {
    click(loanLink);
  }

  public void goToContact() {
    click(contactLink);
  }

  public void clickLogo() {
	    By logo = By.cssSelector("div#headerPanel a[href*='index.htm']");
	    wait.until(ExpectedConditions.elementToBeClickable(logo));
	    click(logo);
	}



  public void clickNavLink(String linkText) {
	    By navLink = By.xpath("//a[contains(text(),'" + linkText + "')]");
	    safeSleep(1000); // Stabilize DOM
	    wait.until(ExpectedConditions.presenceOfElementLocated(navLink));
	    wait.until(ExpectedConditions.elementToBeClickable(navLink));
	    click(navLink);
	}


}

