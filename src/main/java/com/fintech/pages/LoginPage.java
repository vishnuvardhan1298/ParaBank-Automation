package com.fintech.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.File;
import org.apache.commons.io.FileUtils;
import java.time.Duration;
import com.fintech.utils.ConfigReader;
import org.openqa.selenium.WebElement;
 

public class LoginPage extends BasePage {
  

  private By usernameField = By.name("username");
  private By passwordField = By.name("password");
  private By loginButton = By.cssSelector("input[type='submit'][value='Log In']");

  private By logoutButton = By.linkText("Log Out");
  private By errorMessage = By.cssSelector("#rightPanel .error, #leftPanel p");
  private By accountOverviewHeader = By.xpath("//*[contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'ACCOUNTS')]");

  public LoginPage(WebDriver driver) { 
	  super(driver); 
  }
  public void enterUsername(String username) {
	    type(usernameField, username);
	}

	public void enterPassword(String password) {
	    type(passwordField, password);
	}

	public void clickLogin() {
	    click(loginButton);
	}

  public void open() {
	    String url = ConfigReader.get("baseUrl");
	    System.out.println("Resolved base URL: " + url);


	    try {
	        navigateTo(url);
	        System.out.println("Title: " + driver.getTitle()); // ✅ Add this here
	    } catch (WebDriverException e) {
	        throw new AssertionError("Network issue: Unable to reach ParaBank → " + url, e);
	    }
	 // 🔧 Fallback navigation before retries
	    driver.get(url);
	    safeSleep(1000);

	    for (int attempt = 0; attempt < 5; attempt++) {
	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
	        try {
	            wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
	            boolean isLoaded = driver.findElements(By.name("username")).size() > 0;
	            if (isLoaded) {
	                System.out.println("Login page loaded successfully on attempt " + (attempt + 1));
	                return;
	            }
	        } catch (Exception e) {
	        	System.out.println("Login page not ready, retrying... (" + (attempt + 1) + ")");
	        	driver.navigate().refresh();
	        	safeSleep(1000);
	        	try {
	        	    navigateTo(url);
	        	} catch (WebDriverException ex) {
	        	    throw new AssertionError("Network issue during retry → " + url, ex);
	        	}
	        	safeSleep(1500);

	        }
	    }

	    System.out.println("Final attempt → page source:\n" + driver.getPageSource());
	    System.out.println("Current URL: " + driver.getCurrentUrl());
	    System.out.println("Page title: " + driver.getTitle());
	    try {
	        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
	        FileUtils.copyFile(screenshot, new File("screenshots/login_load_failure.png"));
	    } catch (Exception ex) {
	        System.out.println("Screenshot capture failed: " + ex.getMessage());
	    }


	    throw new AssertionError("Login page failed to load after retries");
	}



  public void login(String username, String password) {
    open();
 // ✅ Add these two lines right here
    System.out.println("Login attempt → username: " + username + ", password: " + password);
    System.out.println("Current URL: " + driver.getCurrentUrl());
 // ✅ Add this block immediately after open()
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));

    try {
      wait.until(ExpectedConditions.presenceOfElementLocated(usernameField));
      wait.until(ExpectedConditions.presenceOfElementLocated(loginButton));
      wait.until(ExpectedConditions.elementToBeClickable(loginButton));

      type(usernameField, username);
      type(passwordField, password);

      try {
        click(loginButton);
      } catch (Exception e) {
        jsClick(loginButton);
      }

      By accountTable = By.id("accountTable");
      By logoutLink = By.linkText("Log Out");
      boolean retried = false;

      for (int attempt = 0; attempt < 3; attempt++) {
        try {
          new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(accountTable),
            ExpectedConditions.visibilityOfElementLocated(logoutLink),
            ExpectedConditions.urlContains("overview")
          ));
        } catch (Exception ignored) {}

        if (!isLoginSuccessful() && !retried) {
          retried = true;
          driver.navigate().refresh();
          safeSleep(400);

          wait.until(ExpectedConditions.presenceOfElementLocated(usernameField));
          wait.until(ExpectedConditions.presenceOfElementLocated(loginButton));
          wait.until(ExpectedConditions.elementToBeClickable(loginButton));

          type(usernameField, username);
          type(passwordField, password);
          click(loginButton);
          continue;
        }

        break;
      }

      safeSleep(1000);

      if (!isLoginSuccessful()) {
        if (isErrorVisible()) {
          System.out.println("Login failed as expected. Error: " + getErrorText());
          return; // ✅ allow test to assert error later
        }

        System.out.println("DEBUG page source:\n" + driver.getPageSource());
        try {
          File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
          FileUtils.copyFile(screenshot, new File("screenshots/login_failure.png"));
        } catch (Exception ex) {
          System.out.println("Screenshot capture failed: " + ex.getMessage());
        }

        dumpDebug();
        throw new AssertionError("Login failed — no success or error message found");
      }

      } catch (Exception e) { // ✅ this is the missing catch block
        System.out.println("Unexpected error during login: " + e.getMessage());
        throw new AssertionError("Login failed due to unexpected error");
      } // ✅ closes the try-catch
    } // ✅ closes the login method


  public boolean isLogoutVisible() { return isDisplayed(logoutButton); }

  public boolean isErrorVisible() { return isDisplayed(errorMessage); }

  public String getErrorText() { return getText(errorMessage); }

  public boolean isPasswordMasked() {
	  try {
	    WebElement passwordField = new WebDriverWait(driver, Duration.ofSeconds(10))
	      .until(ExpectedConditions.presenceOfElementLocated(By.name("password")));
	    return "password".equals(passwordField.getAttribute("type"));
	  } catch (Exception e) {
	    return false;
	  }
	  
	}
  public void clickRememberMe() {
	    By rememberLocator = By.cssSelector("input[type='checkbox'][name*='remember']");
	    if (exists(rememberLocator)) click(rememberLocator);
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

  public boolean isLoginSuccessful() {
	    try {
	        System.out.println("Login check → URL: " + driver.getCurrentUrl());
	        System.out.println("Login check → Title: " + driver.getTitle());

	        if (exists(By.id("accountTable"))) {
	            System.out.println("Login check → accountTable found");
	            return true;
	        }
	        if (exists(accountOverviewHeader)) {
	            System.out.println("Login check → accountOverviewHeader found");
	            return true;
	        }
	        if (exists(logoutButton)) {
	            System.out.println("Login check → logoutButton found");
	            return true;
	        }

	        String url = driver.getCurrentUrl().toLowerCase();
	        String title = driver.getTitle().toLowerCase();

	        return url.contains("overview") || title.contains("accounts");
	    } catch (Exception e) {
	        return false;
	    }
	}

  public void dumpDebug() {
	    System.out.println("LoginPage → URL: " + driver.getCurrentUrl());
	    System.out.println("LoginPage → Title: " + driver.getTitle());
	    try {
	        WebElement error = driver.findElement(By.cssSelector(".error"));
	        System.out.println("LoginPage → Error: " + error.getText());
	    } catch (Exception ignored) {}
	}
    
    public boolean exists(By locator) { return driver.findElements(locator).size() > 0; }

}

