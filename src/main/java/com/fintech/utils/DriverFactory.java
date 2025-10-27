package com.fintech.utils;

import com.fintech.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DriverFactory {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver getDriver(String browser) {
        if (driver.get() == null) {
            boolean headless = Boolean.parseBoolean(ConfigReader.get("headless"));

            if (browser.equalsIgnoreCase("chrome")) {
                ChromeOptions options = new ChromeOptions();
                if (headless) options.addArguments("--headless=new");
                driver.set(new ChromeDriver(options));
            } else if (browser.equalsIgnoreCase("edge")) {
                EdgeOptions options = new EdgeOptions();
                if (headless) options.addArguments("--headless=new");
                driver.set(new EdgeDriver(options));
            } else {
                throw new RuntimeException("Unsupported browser: " + browser);
            }
        }
        return driver.get();
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            try {
                if (((RemoteWebDriver) driver.get()).getSessionId() != null) {
                    System.out.println("✅ driver.quit() called for: " + driver.get().getClass().getSimpleName());
                    driver.get().quit();
                } else {
                    System.out.println("⚠️ Session already null — skipping quit");
                }
            } catch (Exception e) {
                System.out.println("❌ Error during driver quit: " + e.getMessage());
            } finally {
                driver.remove(); // ✅ This is the correct place
                System.out.println("🧹 ThreadLocal driver reference removed");
            }
        }
    }
}

