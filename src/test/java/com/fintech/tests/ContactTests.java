package com.fintech.tests;

import com.fintech.pages.ContactPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ContactTests extends BaseTest {

  @DataProvider(name = "contactInputs")
  public Object[][] contactInputs() {
    return new Object[][] {
      {"Test User", "test@example.com", "9999999999", "This is a test message", true},
      {"", "", "", "", false}
    };
  }

  @Test(dataProvider = "contactInputs")
  public void submitContactForm(String name, String email, String phone, String message, boolean expectSuccess) {
    loginAsJohn();
    ContactPage cp = new ContactPage(driver);
    cp.enterName(name);
    cp.enterEmail(email);
    cp.enterPhone(phone);
    cp.enterMessage(message);
    cp.clickSend();

    if (expectSuccess) {
      Assert.assertTrue(cp.isSubmitted(), "Expected form to submit");
    } else {
      Assert.assertTrue(!cp.getResponse().isEmpty(), "Expected validation message");
    }

    test.pass("Contact form scenario validated");
  }
}
