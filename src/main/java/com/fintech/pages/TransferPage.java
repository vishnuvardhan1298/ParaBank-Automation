package com.fintech.pages;

import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.*;

public class TransferPage extends BasePage {

    @FindBy(id = "fromAccountId")
    private WebElement fromAccount;

    @FindBy(id = "toAccountId")
    private WebElement toAccount;

    @FindBy(id = "amount")
    private WebElement amountField;

    @FindBy(xpath = "//input[@value='Transfer' or @type='submit']")
    private WebElement transferBtn;

    @FindBy(xpath = "//h1|//div[contains(@class,'result') or contains(@class,'title')]")
    private WebElement confirmation;

    @FindBy(css = "#rightPanel .error")
    private WebElement errorMsg;

    public TransferPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @Override
    public boolean isLoaded() {
        return isTransferFormLoaded();
    }

    public boolean isTransferFormLoaded() {
        return isDisplayed(fromAccount) && isDisplayed(toAccount) && isDisplayed(transferBtn);
    }

    public TransferPage open() {
        navigateTo("https://parabank.parasoft.com/parabank/transfer.htm");

        for (int i = 0; i < 3; i++) {
            if (isDisplayed(toAccount)) return this;
            System.out.println("🔁 Transfer page not ready — retrying (" + (i + 1) + ")");
            driver.navigate().refresh();
            safeSleep(1000);
        }

        if (!isDisplayed(toAccount)) {
            captureScreenshot("TransferPageLoadFailure");
            throw new AssertionError("❌ Transfer page did not load — toAccountId missing");
        }

        System.out.println("✅ Transfer page loaded successfully.");
        return this;
    }

    public TransferPage enterAmount(String amt) {
        type(amountField, amt);
        return this;
    }

    public TransferPage clickTransfer() {
        click(transferBtn);
        return this;
    }

    public TransferPage selectFromByVisibleText(String text) {
        selectDropdown(fromAccount, text);
        return this;
    }

    public TransferPage selectToByVisibleText(String text) {
        selectDropdown(toAccount, text);
        return this;
    }

    public List<String> getToAccountDropdownOptions() {
        try {
            waitForVisibility(toAccount);
            return toAccount.findElements(By.tagName("option")).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
        } catch (TimeoutException e) {
            captureScreenshot("ToAccountDropdownMissing");
            throw new AssertionError("❌ toAccountId dropdown not found", e);
        }
    }

    public TransferPage selectOptionStartingWith(WebElement dropdownElement, String partialText) {
        Select select = new Select(dropdownElement);
        for (int i = 0; i < 5; i++) {
            if (select.getOptions().size() > 1) break;
            safeSleep(1000);
            select = new Select(dropdownElement);
        }

        for (WebElement option : select.getOptions()) {
            if (option.getText().trim().startsWith(partialText)) {
                select.selectByVisibleText(option.getText());
                System.out.println("✅ Selected option: " + option.getText());
                return this;
            }
        }

        captureScreenshot("MissingOption_" + partialText);
        System.out.println("⚠️ Option starting with '" + partialText + "' not found — using fallback");

        if (!select.getOptions().isEmpty()) {
            WebElement fallback = select.getOptions().get(0);
            select.selectByVisibleText(fallback.getText());
            System.out.println("✅ Fallback selected: " + fallback.getText());
        } else {
            throw new AssertionError("❌ No options available in dropdown");
        }

        return this;
    }

    public TransferPage transferFunds(String from, String to, double amount) {
        click(By.linkText("Transfer Funds"));
        waitForVisibility(fromAccount);

        selectOptionStartingWith(fromAccount, from);

        List<WebElement> options = toAccount.findElements(By.tagName("option"));
        boolean toExists = options.stream().anyMatch(o -> o.getText().startsWith(to));

        if (toExists) {
            selectOptionStartingWith(toAccount, to);
        } else {
            captureScreenshot("MissingOption_" + to);
            throw new AssertionError("❌ Destination account not found: " + to);
        }

        System.out.printf("✅ Transfer: From %s → To %s | Amount: %.2f%n", from, to, amount);
        enterAmount(String.valueOf(amount));
        clickTransfer();
        return this;
    }

    public boolean isTransferConfirmed() {
        try {
            WebElement confirmationElement = wait.until(ExpectedConditions.visibilityOf(confirmation));
            String text = confirmationElement.getText().trim();
            System.out.println("✅ Transfer confirmation text: " + text);

            boolean hasSuccessMsg = driver.findElements(By.cssSelector(".success, .confirmation")).stream()
                .anyMatch(WebElement::isDisplayed);
            System.out.println("✅ Success message visible: " + hasSuccessMsg);

            return confirmationElement.isDisplayed() && text.toLowerCase().contains("transfer complete");
        } catch (Exception e) {
            captureScreenshot("TransferConfirmationMissing");
            System.out.println("❌ Transfer confirmation not found: " + e.getMessage());
            return false;
        }
    }
    public String getConfirmationText() {
        try {
            waitForVisibility(confirmation);
            String text = confirmation.getText().trim();
            System.out.println("🔍 Confirmation text: " + text);
            return text;
        } catch (Exception e) {
            captureScreenshot("ConfirmationTextMissing");
            System.out.println("❌ Failed to fetch confirmation text: " + e.getMessage());
            return "";
        }
    }

    public String getError() {
        return getText(errorMsg);
    }
}

