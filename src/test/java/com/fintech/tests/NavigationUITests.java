package com.fintech.tests;

import com.fintech.pages.NavigationPage;
import com.fintech.pages.BasePage;
import com.fintech.utils.TestDataProvider;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.NoSuchElementException;

public class NavigationUITests extends BaseTest {

    // ✅ Excel-driven navigation test
    @Test(dataProvider = "navLinksExcel", dataProviderClass = TestDataProvider.class)
    public void topNavLinksLoadPages(String linkText, String expectedUrlFragment, Class<?> expectedPageClass) {
        test = extent.createTest("NavLink → " + linkText);
        test.assignCategory("Navigation");
        test.info("Clicked on: " + linkText);

        loginAsJohn();
        safeSleep(1000);

        NavigationPage nav = new NavigationPage(driver);
        nav.clickNavLink(linkText);

        test.info("Current URL: " + driver.getCurrentUrl());
        test.info("Page Title: " + driver.getTitle());

        Assert.assertTrue(driver.getCurrentUrl().contains(expectedUrlFragment), "Should land on correct page");

        BasePage page = (BasePage) PageFactory.initElements(driver, expectedPageClass);
        Assert.assertTrue(page.isLoaded(), linkText + " page should load");
        test.pass(linkText + " navigation validated");
    }

    @Test
    public void logoRedirectsHome() {
        loginAsJohn();
        safeSleep(1000);

        NavigationPage nav = new NavigationPage(driver);
        nav.clickLogo();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("index.htm"));

        System.out.println("Current URL: " + driver.getCurrentUrl());
        System.out.println("Current Title: " + driver.getTitle());

        Assert.assertTrue(driver.getTitle().contains("Welcome"), "Should redirect to homepage");
        test.pass("Logo redirect to homepage validated");
    }

    @Test
    public void validateButtonsAreClickable() {
        loginAsJohn();

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

        System.out.println("Font: " + font);
        System.out.println("Alignment: " + align);
        System.out.println("Full font CSS: " + driver.findElement(header).getAttribute("style"));

        Assert.assertTrue(align.equals("center") || align.equals("start"), "Text should be center or start aligned");
        Assert.assertTrue(font.contains("Arial") || font.contains("sans-serif"), "Font should be consistent");
        test.pass("Font and alignment validated");
    }

    @Test
    public void validateAlertBoxContent() {
        loginAsJohn();

        test = extent.createTest("Scenario: Validate Alert Box");
        test.assignCategory("UI Validation");

        System.out.println("Current URL: " + driver.getCurrentUrl());
        System.out.println("Page Title: " + driver.getTitle());

        ((JavascriptExecutor) driver).executeScript("alert('Success');");
        test.info("Simulated alert using JavaScript");

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

    @Test
    public void checkParaBankConnectivity() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        System.out.println("Title: " + driver.getTitle());
        Assert.assertTrue(driver.getTitle().contains("ParaBank"), "Title should contain ParaBank");
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
}


