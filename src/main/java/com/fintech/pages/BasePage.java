package com.fintech.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.List;

public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void navigateTo(String url) {
        driver.get(url);
    }

    public void safeSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(text);
    }

    public void click(By locator) {
        System.out.println("Clicking → " + locator.toString());
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        el.click();
    }


    public void jsClick(By locator) {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    public boolean isDisplayed(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }


    public String getText(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }


    public boolean exists(By locator) {
        return driver.findElements(locator).size() > 0;
    }
    public List<WebElement> findAll(By locator) {
        try {
            wait.until(driver -> !driver.findElements(locator).isEmpty());
            return driver.findElements(locator);
        } catch (Exception e) {
            return driver.findElements(locator);
        }
    }
    public void waitForClickable(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public void waitForClickable(By locator, Duration timeout) {
        new WebDriverWait(driver, timeout).until(ExpectedConditions.elementToBeClickable(locator));
    }
    public void selectDropdown(By locator, String visibleText) {
        WebElement dropdown = find(locator);
        new Select(dropdown).selectByVisibleText(visibleText);
    }
    public WebElement find(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    public void waitForVisible(By locator, Duration timeout) {
        new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    public boolean isLoaded() {
        return true; // Default implementation; override in each page class
    }
    public String getFontFamily(By locator) {
        return driver.findElement(locator).getCssValue("font-family");
    }

    public String getTextAlign(By locator) {
        return driver.findElement(locator).getCssValue("text-align");
    }
}
