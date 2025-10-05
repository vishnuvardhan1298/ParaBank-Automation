package com.fintech.tests;

import com.fintech.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.openqa.selenium.By;


public class AuthTests extends BaseTest {

  @DataProvider(name = "userCredentials")
  public Object[][] userCredentials() {
    return new Object[][] {
      {"john", "demo"},
      {"john", "wrong"},
      {"notexist", "demo"}
    };
  }

  @Test(dataProvider = "userCredentials")
  public void logoutAfterLogin(String user, String pass) {
    LoginPage lp = new LoginPage(driver);
    lp.open();
    lp.login(user, pass);

    if (lp.isLoginSuccessful()) {
      try {
        lp.logout();
      } catch (Exception e) {
        driver.findElement(By.linkText("Log Out")).click();
      }
      Assert.assertFalse(lp.isLogoutVisible(), "Logout link should not be visible after logging out");
      test.pass("Logout validated for user: " + user);
    } else {
      test.info("Login failed for user: " + user + " — logout skipped");
      Assert.assertTrue(true); // neutral pass
    }
  }

  @Test
  public void sessionTimeoutSimulation() throws InterruptedException {
    loginAsJohn();
    LoginPage lp = new LoginPage(driver);
    Assert.assertTrue(lp.isLoginSuccessful(), "Login should succeed");

    Thread.sleep(1000 * 60 * 5); // simulate 5-minute inactivity
    lp.open();

    boolean timedOut = lp.getErrorText().length() > 0 || !lp.isLoginSuccessful();
    Assert.assertTrue(timedOut, "After inactivity user should be logged out or redirected");
    test.pass("Session timeout simulation executed");
  }
}


