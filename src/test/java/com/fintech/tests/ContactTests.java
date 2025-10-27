package com.fintech.tests;

import com.fintech.pages.ContactPage;
import com.fintech.utils.TestDataProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ContactTests extends BaseTest {

  // ✅ Scenario 1: Open Contact Form
  @Test
  public void openContactForm() {
    loginAsJohn();
    ContactPage cp = new ContactPage(driver);
    cp.openForm();
    Assert.assertTrue(cp.isLoaded(), "Contact form did not load.");
    test.pass("Contact form opened successfully.");
  }

  // ✅ Scenario 2 & 3: Submit valid and empty forms from Excel
  @Test(dataProvider = "contactDataExcel", dataProviderClass = TestDataProvider.class)
  public void submitContactForm(String name, String email, Double phone, String message, Boolean expectSuccess) {
      String phoneStr = phone == null ? "" : String.valueOf(phone.longValue());

      loginAsJohn();
      ContactPage cp = new ContactPage(driver);
      cp.openForm();
      cp.enterName(name);
      cp.enterEmail(email);
      cp.enterPhone(phoneStr);
      cp.enterMessage(message);
      cp.clickSend();

      if (expectSuccess) {
          Assert.assertTrue(cp.isSubmitted(), "Expected form to submit");
      } else {
          Assert.assertTrue(!cp.getResponse().isEmpty(), "Expected validation message");
      }

      test.pass("Contact form scenario validated for: " + name);
  }

  // ✅ Scenario 4: Verify success message
  @Test
  public void verifyContactSuccessMessage() {
    loginAsJohn();
    ContactPage cp = new ContactPage(driver);
    cp.openForm();

    cp.enterName("Vishnu");
    cp.enterEmail("vishnu@example.com");
    cp.enterPhone("9876543210");
    cp.enterMessage("Need help with account.");
    cp.clickSend();

    String msg = cp.getResponse().toLowerCase();
    System.out.println("Received message: " + msg);

    boolean isSuccess = msg.contains("thank") || msg.contains("sent") || msg.contains("submitted") || msg.contains("we will") || msg.contains("success");
    Assert.assertTrue(isSuccess, "Success message not shown. Actual: " + msg);

    test.pass("Success message verified: " + msg);
  }
}

