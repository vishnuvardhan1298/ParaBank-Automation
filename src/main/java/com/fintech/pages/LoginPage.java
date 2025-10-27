package com.fintech.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import com.fintech.utils.ConfigReader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoginPage extends BasePage {

    @FindBy(name = "username")
    private WebElement usernameField;

    @FindBy(name = "password")
    private WebElement passwordField;

    @FindBy(css = "input[type='submit'][value='Log In']")
    private WebElement loginButton;

    @FindBy(linkText = "Log Out")
    private WebElement logoutButton;

    @FindBy(css = "#rightPanel .error, #leftPanel p")
    private WebElement errorMessage;

    @FindBy(xpath = "//*[contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'ACCOUNTS')]")
    private WebElement accountOverviewHeader;

    public LoginPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @Override
    public boolean isLoaded() {
        return driver.getTitle().toLowerCase().contains("parabank") &&
               isDisplayed(usernameField) &&
               isDisplayed(passwordField) &&
               isDisplayed(loginButton);
    }

    public LoginPage open() {
        String url = ConfigReader.get("baseUrl");
        System.out.println("Resolved base URL: " + url);

        if (!driver.getCurrentUrl().contains("index.htm")) {
            driver.get("https://parabank.parasoft.com/parabank/index.htm");
        }

        navigateTo(url);
        safeSleep(1000);

        try {
            waitForVisibility(usernameField);
            waitForVisibility(passwordField);
            waitForClickable(loginButton);
        } catch (TimeoutException e) {
            captureScreenshot("LoginPageTimeout");
            dumpDebug();
            throw e;
        }

        return this;
    }

    public LoginPage submitLogin(String username, String password) {
        open();
        System.out.println("Login attempt → username: " + username + ", password: " + password);

        try {
            usernameField.clear();
            usernameField.sendKeys(username);
            passwordField.clear();
            passwordField.sendKeys(password);

            try {
                loginButton.click();
            } catch (Exception e) {
                jsClick(loginButton);
            }

            for (int attempt = 0; attempt < 3; attempt++) {
                if (isLoginSuccessful()) break;

                System.out.println("🔁 Retry login attempt " + (attempt + 1));
                driver.navigate().refresh();
                safeSleep(400);
                usernameField.clear();
                usernameField.sendKeys(username);
                passwordField.clear();
                passwordField.sendKeys(password);
                click(loginButton);
            }

            safeSleep(1000);

            
            System.out.println("Post-login → URL: " + driver.getCurrentUrl());
            System.out.println("Post-login → Title: " + driver.getTitle());
            System.out.println("Post-login → isLoginSuccessful: " + isLoginSuccessful());
            System.out.println("Post-login → isErrorVisible: " + isErrorVisible());

            if (!isLoginSuccessful()) {
                if (isErrorVisible()) {
                    System.out.println("Login failed as expected. Error: " + getErrorText());
                    captureScreenshot("LoginExpectedFailure_" + username);
                } else {
                    captureScreenshot("LoginFailure_" + username);
                    dumpDebug();
                }
            }

        } catch (Exception e) {
            captureScreenshot("LoginUnexpectedError_" + username);
            System.out.println("Login failed due to unexpected error: " + e.getMessage());
        }

        return this;
    }
    public LoginPage login(String username, String password) {
        return submitLogin(username, password);
    }

    public LoginPage enterUsername(String user) {
        type(usernameField, user);
        return this;
    }

    public LoginPage enterPassword(String pass) {
        type(passwordField, pass);
        return this;
    }

    public LoginPage clickLogin() {
        try {
            click(loginButton);
        } catch (Exception e) {
            jsClick(loginButton);
        }
        return this;
    }

    public boolean isLogoutVisible() {
        return isDisplayed(logoutButton);
    }

    public boolean isErrorVisible() {
        return isDisplayed(errorMessage);
    }

    public String getErrorText() {
        return getText(errorMessage);
    }

    public boolean isPasswordMasked() {
        try {
            return "password".equals(passwordField.getAttribute("type"));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLoginSuccessful() {
        try {
            safeSleep(500);

            boolean header = driver.findElements(By.xpath("//*[contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'ACCOUNTS')]"))
                .stream().anyMatch(WebElement::isDisplayed);

            boolean logout = driver.findElements(By.linkText("Log Out"))
                .stream().anyMatch(WebElement::isDisplayed);

            boolean url = driver.getCurrentUrl().toLowerCase().contains("overview");
            boolean title = driver.getTitle().toLowerCase().contains("accounts");

            System.out.println("Login check → header: " + header + ", logout: " + logout +
                               ", url: " + url + ", title: " + title);

            return header || logout || url || title;
        } catch (Exception e) {
            System.out.println("Login check failed: " + e.getMessage());
            return false;
        }
    }

    public void clickRememberMe() {
        By rememberLocator = By.cssSelector("input[type='checkbox'][name*='remember']");
        if (exists(rememberLocator)) click(rememberLocator);
    }

    public void logout() {
        try {
            click(logoutButton);
        } catch (Exception e) {
            try {
                click(By.linkText("Log Out"));
            } catch (Exception ignored) {}
        }
    }

    public void dumpDebug() {
        System.out.println("LoginPage → URL: " + driver.getCurrentUrl());
        System.out.println("LoginPage → Title: " + driver.getTitle());
        try {
            System.out.println("LoginPage → Error: " + errorMessage.getText());
        } catch (Exception ignored) {}
    }

    public void captureScreenshot(String name) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File dest = new File("target/screenshots", name + "_" + timestamp + ".png");
            FileUtils.copyFile(screenshot, dest);
            System.out.println("📸 Screenshot saved: " + dest.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("⚠️ Screenshot failed: " + e.getMessage());
        }
    }
}

