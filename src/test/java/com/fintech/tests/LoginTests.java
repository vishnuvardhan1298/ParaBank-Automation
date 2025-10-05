package com.fintech.tests;

import com.fintech.pages.LoginPage;
import com.fintech.pages.AccountPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.fintech.utils.DriverFactory;
import com.fintech.utils.ConfigReader;



import java.util.Set;

public class LoginTests extends BaseTest {
  

  @DataProvider(name = "loginData")
  public Object[][] loginData() {
    return new Object[][] {
      {"john", "demo", "SUCCESS"},
      {"john", "wrong", "INVALID"},
      {"notexist", "any", "USER_NOT_EXIST"},
      {"", "", "EMPTY"},
      {"user@com", "pass", "INVALID_EMAIL"}
    };
  }

  @Test(dataProvider = "loginData", groups = "login", retryAnalyzer = com.fintech.utils.RetryAnalyzer.class)
  public void loginScenarios(String user, String pass, String expected) {
	System.out.println("Test case: " + expected + " → user: " + user + ", pass: " + pass);  
    LoginPage lp = new LoginPage(driver);
    lp.open();
    lp.login(user, pass);

    if ("SUCCESS".equals(expected)) {
      Assert.assertTrue(new AccountPage(driver).isLoaded(), "Account summary should load on valid login");
      test.pass("Login success validated");
      lp.logout();
      safeSleep(1000);
      lp.open();
    } else {
      Assert.assertTrue(lp.isErrorVisible(), "Error message should be visible for: " + expected);
      test.pass("Login negative scenario validated: " + expected);
      lp.open();
    }
  }

  @Test(groups = "login")
  public void passwordMasking() {
    LoginPage lp = new LoginPage(driver);
    lp.open();
    Assert.assertTrue(lp.isPasswordMasked(), "Password input should be masked");
    test.pass("Password field is properly masked");
  }

  @Test(groups = "login")
  public void sessionTimeoutSimulation() {
    LoginPage lp = new LoginPage(driver);
    lp.open();
    lp.login("john", "demo");

    for (Cookie c : driver.manage().getCookies()) {
      if (c.getName().toLowerCase().contains("session")) {
        driver.manage().deleteCookieNamed(c.getName());
      }
    }

    driver.navigate().refresh();
    safeSleep(1000);
    lp.open();
    Assert.assertFalse(lp.isLoginSuccessful(), "User should be logged out after session removal");
    test.pass("Session timeout simulation validated");
  }

  @Test(groups = "login")
  public void rememberMeSimulation() {
      LoginPage lp = new LoginPage(driver);
      lp.open();
      lp.clickRememberMe(); // ✅ triggers checkbox
      lp.login("john", "demo");

      // 🔄 Simulate tab switch or navigation away
      driver.get("https://parabank.parasoft.com/parabank/services.htm");
      safeSleep(1000);

      // 🔙 Return to login page
      driver.get("https://parabank.parasoft.com/parabank/index.htm");
      safeSleep(1000);

      // ✅ Validate session still active
      Assert.assertTrue(lp.isLoginSuccessful(), "User should remain logged in within session");
      test.pass("Simulated remember-me validated within session");
  }



}



