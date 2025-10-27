package com.fintech.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.*;

import com.fintech.utils.ConfigReader;

import java.time.Duration;
import java.io.File;


public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(getWaitTime()));
        PageFactory.initElements(driver, this); // ✅ Enables @FindBy support
    }

    // 🔹 Configurable wait time
    protected int getWaitTime() {
        return Integer.parseInt(ConfigReader.get("defaultWait", "15"));
    }

    // 🔹 Navigation
    public BasePage navigateTo(String url) {
        driver.get(url);
        return this;
    }

    // 🔹 Fluent sleep
    public BasePage safeSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        return this;
    }

    // 🔹 Fluent typing
    public BasePage type(By locator, String text) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            element.clear();
            element.sendKeys(text);
        } catch (TimeoutException e) {
            captureScreenshot("element_not_visible_" + locator.toString());
            throw new RuntimeException("Timeout waiting for: " + locator, e);
        }
        return this;
    }

    public BasePage type(WebElement element, String text) {
        wait.until(ExpectedConditions.visibilityOf(element));
        element.clear();
        element.sendKeys(text);
        return this;
    }

    // 🔹 Fluent click
    public BasePage click(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        el.click();
        return this;
    }

    public BasePage click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        return this;
    }

    public BasePage jsClick(By locator) {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        return this;
    }

    public BasePage jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        return this;
    }

    // 🔹 Screenshot
    public void captureScreenshot(String name) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File dest = new File("target/screenshots", name + "_" + timestamp + ".png");
            org.apache.commons.io.FileUtils.copyFile(screenshot, dest);
            System.out.println("📸 Screenshot saved: " + dest.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("⚠️ Screenshot failed: " + e.getMessage());
        }
    }

    // 🔹 Visibility and existence
    public boolean isDisplayed(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isDisplayed(WebElement element) {
        try {
            return wait.until(ExpectedConditions.visibilityOf(element)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean exists(By locator) {
        return driver.findElements(locator).size() > 0;
    }

    // 🔹 Text and style
    public String getText(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public String getText(WebElement element) {
        try {
            return wait.until(ExpectedConditions.visibilityOf(element)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public String getFontFamily(By locator) {
        return driver.findElement(locator).getCssValue("font-family");
    }

    public String getTextAlign(By locator) {
        return driver.findElement(locator).getCssValue("text-align");
    }

    // 🔹 Dropdown
    public BasePage selectDropdown(By locator, String visibleText) {
        try {
            Select dropdown = new Select(driver.findElement(locator));
            dropdown.selectByVisibleText(visibleText);
        } catch (NoSuchElementException e) {
            captureScreenshot("DropdownOptionMissing_" + visibleText);
            throw new RuntimeException("Dropdown option '" + visibleText + "' not found", e);
        }
        return this;
    }

    public BasePage selectDropdown(WebElement dropdownElement, String visibleText) {
        try {
            Select dropdown = new Select(dropdownElement);
            dropdown.selectByVisibleText(visibleText);
        } catch (NoSuchElementException e) {
            captureScreenshot("DropdownOptionMissing_" + visibleText);
            throw new RuntimeException("Dropdown option '" + visibleText + "' not found", e);
        }
        return this;
    }

    // 🔹 Waits
    public BasePage waitForClickable(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
        return this;
    }

    public BasePage waitForClickable(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
        return this;
    }

    public BasePage waitForVisibility(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
        return this;
    }

    // 🔹 Debug
    public void dumpDebug(By locator) {
        System.out.println("Page title: " + driver.getTitle());
        System.out.println("Current URL: " + driver.getCurrentUrl());
        System.out.println("Element exists: " + exists(locator));
        System.out.println("Element visible: " + isDisplayed(locator));
    }
    public BasePage waitForVisibility(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        return this;
    }
    public boolean isSessionActive() {
        try {
            return driver != null &&
                   driver.getWindowHandles().size() > 0 &&
                   driver.getCurrentUrl().toLowerCase().contains("parabank");
        } catch (Exception e) {
            System.out.println("⚠️ Session check failed: " + e.getMessage());
            return false;
        }
    }
    
    // 🔹 Page load contract
    public abstract boolean isLoaded(); // Must be overridden in child classes
}
