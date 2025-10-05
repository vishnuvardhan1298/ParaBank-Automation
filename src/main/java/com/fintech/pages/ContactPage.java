package com.fintech.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ContactPage extends BasePage {
  private By name = By.id("name");
  private By email = By.id("email");
  private By phone = By.id("phone");
  private By message = By.id("message");
  private By submitBtn = By.xpath("//input[@value='Send' or @type='submit']");
  private By successMsg = By.cssSelector("#rightPanel .title, #rightPanel .result");
  private By errorMsg = By.cssSelector("#rightPanel .error");

  private By contactHeader = By.xpath("//h1[contains(text(),'Update Profile')]");



  public ContactPage(org.openqa.selenium.WebDriver driver) { super(driver); }

  public void enterName(String n) { type(name, n); }

  public void enterEmail(String e) { type(email, e); }

  public void enterPhone(String p) { type(phone, p); }

  public void enterMessage(String m) { type(message, m); }

  public void clickSend() { click(submitBtn); }

  public String getResponse() {
    String r = getText(successMsg);
    if (r.isEmpty()) r = getText(errorMsg);
    return r;
  }
  

  public boolean isLoaded() {
	    try {
	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	        WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(contactHeader));
	        System.out.println("Header Text: " + header.getText());
	        return header.isDisplayed();
	    } catch (Exception e) {
	        System.out.println("ContactPage isLoaded() failed: " + e.getMessage());
	        return false;
	    }
	}



    public boolean isSubmitted() { return !getResponse().isEmpty(); }
}


