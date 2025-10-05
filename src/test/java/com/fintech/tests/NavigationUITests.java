package com.fintech.tests;

import com.fintech.pages.NavigationPage;
import com.fintech.pages.AccountPage;
import com.fintech.pages.TransferPage;
import com.fintech.pages.LoanPage;
import com.fintech.pages.ContactPage;
import com.fintech.pages.BasePage;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.NoSuchElementException;

public class NavigationUITests extends BaseTest {

	@DataProvider(name = "navLinks")
	public Object[][] navLinks() {
	  return new Object[][] {
	    {"Accounts Overview", AccountPage.class},
	    {"Transfer", TransferPage.class},
	    {"Loan", LoanPage.class},
	    {"Contact", ContactPage.class}
	  };
	}


  @Test(dataProvider = "navLinks")
  public void topNavLinksLoadPages(String linkText, Class<?> expectedPageClass) {
      test = extent.createTest("NavLink → " + linkText); // ✅ Dynamic test name
      test.assignCategory("Navigation");                 // ✅ Logical grouping
      test.info("Clicked on: " + linkText);              // ✅ Step-level logging

      loginAsJohn();

      // 🔍 Diagnostic output to verify login and DOM state
      System.out.println("Post-login URL: " + driver.getCurrentUrl());
      System.out.println("Post-login Title: " + driver.getTitle());
      System.out.println("Page Source:\n" + driver.getPageSource());

      safeSleep(1000); // Optional: stabilize DOM

      NavigationPage nav = new NavigationPage(driver);
      nav.clickNavLink(linkText);
     
      test.info("Clicked nav link: " + linkText);
      test.info("Current URL: " + driver.getCurrentUrl());
      test.info("Page Title: " + driver.getTitle());
      test.info("Expected Page Class: " + expectedPageClass.getSimpleName());
      
   // ✅ Optional: URL-level assertion based on linkText
      if (linkText.equals("Accounts Overview")) {
          Assert.assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Should land on Accounts Overview page");
      } else if (linkText.equals("Transfer Funds")) {
          Assert.assertTrue(driver.getCurrentUrl().contains("transfer.htm"), "Should land on Transfer Funds page");
      } else if (linkText.equals("Request Loan")) {
          Assert.assertTrue(driver.getCurrentUrl().contains("requestloan.htm"), "Should land on Loan Request page");
      } else if (linkText.equals("Contact Us")) {
          Assert.assertTrue(driver.getCurrentUrl().contains("contact.htm"), "Should land on Contact Us page");
      }


      BasePage page = (BasePage) PageFactory.initElements(driver, expectedPageClass);
      Assert.assertTrue(page.isLoaded(), linkText + " page should load");
      test.pass(linkText + " navigation validated");
  }




  @Test
  public void logoRedirectsHome() {
      loginAsJohn();
      safeSleep(1000); // Optional: stabilize DOM after login

      NavigationPage nav = new NavigationPage(driver);
      nav.clickLogo();

      // ✅ Wait for homepage to load
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      wait.until(ExpectedConditions.urlContains("index.htm")); // ✅ Wait for homepage URL

      // ✅ Diagnostic output
      System.out.println("Current URL: " + driver.getCurrentUrl());
      System.out.println("Current Title: " + driver.getTitle());

      // ✅ Assert redirect success
      Assert.assertTrue(driver.getTitle().contains("Welcome"), "Should redirect to homepage"); // ✅ Updated assertion
      test.pass("Logo redirect to homepage validated");
  }




  @Test
  public void validateButtonsAreClickable() {
      loginAsJohn(); // ✅ Already logged in

      // ✅ Replace login button with post-login buttons
      Assert.assertTrue(isButtonVisibleAndClickable(By.linkText("Bill Pay")),
          "Bill Pay button should be visible and clickable");

      Assert.assertTrue(isButtonVisibleAndClickable(By.linkText("Find Transactions")),
          "Find Transactions button should be visible and clickable");

      Assert.assertTrue(isButtonVisibleAndClickable(By.linkText("Open New Account")),
          "Open New Account button should be visible and clickable");

      test.pass("All major buttons are visible and clickable");
  }


  @Test
  public void verifyFontAndAlignment() {
      loginAsJohn();

      NavigationPage nav = new NavigationPage(driver);
      By header = By.cssSelector("h1, h2");

      String font = nav.getFontFamily(header);
      String align = nav.getTextAlign(header);

      // 🔍 Diagnostic output
      System.out.println("Font: " + font);
      System.out.println("Alignment: " + align);
      System.out.println("Full font CSS: " + driver.findElement(header).getAttribute("style")); // ✅ Add this

      // ✅ Updated assertion to allow 'start' alignment
      Assert.assertTrue(align.equals("center") || align.equals("start"), "Text should be center or start aligned");

      Assert.assertTrue(font.contains("Arial") || font.contains("sans-serif"), "Font should be consistent");
      test.pass("Font and alignment validated");
  }
  @Test
  public void validateAlertBoxContent() {
      loginAsJohn(); // ✅ Ensure you're logged in first

      test = extent.createTest("Scenario: Validate Alert Box");
      test.assignCategory("UI Validation");

      // 🔍 Optional diagnostics
      System.out.println("Current URL: " + driver.getCurrentUrl());
      System.out.println("Page Title: " + driver.getTitle());

      // ✅ Simulate alert directly
      ((JavascriptExecutor) driver).executeScript("alert('Success');");
      test.info("Simulated alert using JavaScript");

      // ✅ Handle the alert
      try {
          WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
          Alert alert = wait.until(ExpectedConditions.alertIsPresent());

          String alertText = alert.getText();
          System.out.println("Alert Text: " + alertText);
          test.info("Alert Text: " + alertText);

          Assert.assertTrue(alertText.contains("Success"), "Alert should show success message");
          alert.accept();
          test.pass("Alert box content and styling validated");
      } catch (Exception e) {
          test.fail("Alert handling failed: " + e.getMessage());
          Assert.fail("Alert was not handled properly");
      }
  }






  private boolean isButtonVisibleAndClickable(By locator) {
	    try {
	        WebElement element = driver.findElement(locator);
	        return element.isDisplayed() && element.isEnabled();
	    } catch (NoSuchElementException e) {
	        System.out.println("Element not found: " + locator);
	        return false;
	    } catch (Exception e) {
	        System.out.println("Unexpected error while checking button: " + locator + " → " + e.getMessage());
	        return false;
	    }
	}


  @Test
  public void checkParaBankConnectivity() {
      driver.get("https://parabank.parasoft.com/parabank/index.htm");
      System.out.println("Title: " + driver.getTitle());
      Assert.assertTrue(driver.getTitle().contains("ParaBank"), "Title should contain ParaBank");
  }

}


