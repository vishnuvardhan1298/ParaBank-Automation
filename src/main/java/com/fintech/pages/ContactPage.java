package com.fintech.pages;

import java.util.List;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ContactPage extends BasePage {

    @FindBy(id = "name")
    private WebElement nameField;

    @FindBy(id = "email")
    private WebElement emailField;

    @FindBy(id = "phone")
    private WebElement phoneField;

    @FindBy(id = "message")
    private WebElement messageField;

    @FindBy(xpath = "//input[@value='Send' or @type='submit']")
    private WebElement submitButton;

    @FindBy(css = "#rightPanel .title, #rightPanel .result")
    private WebElement successMessage;

    @FindBy(css = "#rightPanel .error")
    private WebElement errorMessage;

    @FindBy(xpath = "//h1[contains(text(),'Customer Care')]")
    private WebElement contactHeader;

    public ContactPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @Override
    public boolean isLoaded() {
        return isDisplayed(contactHeader);
    }

    public ContactPage openForm() {
        click(By.linkText("Contact Us"));
        waitForVisibility(nameField);
        System.out.println("✅ Contact form opened.");
        return this;
    }

    public ContactPage enterName(String name) {
        type(nameField, name);
        return this;
    }

    public ContactPage enterEmail(String email) {
        type(emailField, email);
        return this;
    }

    public ContactPage enterPhone(String phone) {
        type(phoneField, phone);
        return this;
    }

    public ContactPage enterMessage(String message) {
        type(messageField, message);
        return this;
    }

    public ContactPage clickSend() {
        click(submitButton);
        return this;
    }

    public String getResponse() {
        String response = getText(successMessage);
        if (response.isEmpty()) response = getText(errorMessage);
        if (response.equalsIgnoreCase("customer care")) response = "";

        if (response.isEmpty()) {
            try {
                List<WebElement> candidates = driver.findElements(By.cssSelector("#rightPanel p, #rightPanel div"));
                for (WebElement el : candidates) {
                    String text = el.getText().trim();
                    if (!text.equalsIgnoreCase("customer care") && !text.isEmpty()) {
                        response = text;
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }

        if (response.isEmpty()) {
            captureScreenshot("ContactResponseMissing");
        }

        return response;
    }

    public boolean isSubmitted() {
        return !getResponse().isEmpty();
    }
}

