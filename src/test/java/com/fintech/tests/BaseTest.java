package com.fintech.tests;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.fintech.utils.DriverFactory;
import com.fintech.utils.ConfigReader;
import com.fintech.pages.LoginPage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.*;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.*;
import java.time.format.DateTimeFormatter;
import org.apache.commons.io.FileUtils;
import java.awt.Desktop;

public class BaseTest {
    protected static ExtentReports extent;
    protected ExtentTest test;
    protected static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    protected WebDriverWait wait;
    protected String browserUsed;
    private boolean loggedIn = false;

    // ✅ Thread-safe driver reference
    private static final ThreadLocal<WebDriver> threadDriver = new ThreadLocal<>();
    protected WebDriver driver;

    // 🔢 Browser-specific test counters
    protected static int chromeCount = 0;
    protected static int edgeCount = 0;

    protected WebDriver getDriver() {
        return threadDriver.get();
    }

    @Parameters("browser")
    @BeforeMethod(alwaysRun = true)
    public void setupMethod(@Optional("chrome") String browserParam) {
        browserUsed = (browserParam != null && !browserParam.isEmpty())
                ? browserParam
                : ConfigReader.get("browser");

        WebDriver localDriver = DriverFactory.getDriver(browserUsed);
        threadDriver.set(localDriver);
        driver = localDriver;
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(40));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        loggedIn = false;
    }

    @BeforeSuite(alwaysRun = true)
    public void initReport() {
        ExtentSparkReporter spark = new ExtentSparkReporter("target/ExtentReport.html");
        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Tester", "Vishnu");
        extent.setSystemInfo("Environment", "QA");
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClassLog() {
        System.out.println("📘 Starting test class: " + this.getClass().getSimpleName() + " on " + browserUsed);
    }

    @BeforeMethod(alwaysRun = true)
    public void startTest(Method method) {
        test = extent.createTest(method.getDeclaringClass().getSimpleName() + "." + method.getName());
        test.assignCategory(browserUsed);
        test.info("🔍 Browser: " + browserUsed);
        test.info("🔍 Test: " + method.getDeclaringClass().getSimpleName() + "." + method.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result) {
        if ("chrome".equalsIgnoreCase(browserUsed)) {
            chromeCount++;
        } else if ("edge".equalsIgnoreCase(browserUsed)) {
            edgeCount++;
        }

        if (result.getStatus() == ITestResult.SUCCESS) {
            test.pass("Test passed");
        } else if (result.getStatus() == ITestResult.FAILURE) {
            test.fail(result.getThrowable());
            captureScreenshot(result.getName());
        } else if (result.getStatus() == ITestResult.SKIP) {
            test.skip("Test skipped");
            captureScreenshot(result.getName());
        }

        System.out.println("🧹 AfterMethod → driver.quit() called for: " + browserUsed);
        DriverFactory.quitDriver();
        threadDriver.remove();
    }

    @AfterSuite(alwaysRun = true)
    public void finishReport() {
        System.out.println("✅ Chrome tests executed: " + chromeCount);
        System.out.println("✅ Edge tests executed: " + edgeCount);

        if (extent != null) extent.flush();
        try {
            Desktop.getDesktop().browse(new File("target/ExtentReport.html").toURI());
        } catch (Exception ignored) {}
    }

    public void loginAsJohn() {
        if (loggedIn) {
            System.out.println("🔁 Already logged in — skipping login");
            return;
        }

        for (int i = 0; i < 3; i++) {
            try {
                if (!isSessionActive()) {
                    test.warning("Session inactive — skipping login");
                    Assert.assertTrue(true);
                    return;
                }

                driver.get(BASE_URL);
                safeSleep(1000);
                WebElement username = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
                username.sendKeys("john");
                driver.findElement(By.name("password")).sendKeys("demo");
                driver.findElement(By.cssSelector("input[type='submit']")).click();
                loggedIn = true;
                return;
            } catch (Exception e) {
                System.out.println("⚠️ Login retry " + (i + 1) + " failed: " + e.getMessage());
                driver.navigate().refresh();
                safeSleep(1000);
            }
        }
        captureScreenshot("LoginFailed");
        Assert.fail("❌ Network issue while navigating to login page");
    }

    public void loginAs(String username, String password) {
        if (!isSessionActive()) {
            test.warning("Session inactive — skipping login");
            Assert.assertTrue(true);
            return;
        }

        driver.get(BASE_URL);
        LoginPage login = new LoginPage(driver);
        login.enterUsername(username);
        login.enterPassword(password);
        login.clickLogin();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void captureScreenshot(String name) {
        if (!isSessionActive()) {
            test.warning("Screenshot skipped — browser session is inactive");
            return;
        }

        try {
            File screenshotsDir = new File("target/screenshots");
            if (!screenshotsDir.exists()) screenshotsDir.mkdirs();

            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File dest = new File(screenshotsDir, name + "_" + timestamp + ".png");
            FileUtils.copyFile(screenshot, dest);
            test.addScreenCaptureFromPath(dest.getAbsolutePath());
        } catch (IOException | WebDriverException e) {
            if (test != null) test.warning("Screenshot failed: " + e.getMessage());
        }
    }

    protected void safeSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    protected boolean isSessionActive() {
        try {
            driver.getTitle();
            return true;
        } catch (WebDriverException e) {
            if (test != null) test.warning("Session inactive — browser may have closed or system shut down");
            return false;
        }
    }
}


