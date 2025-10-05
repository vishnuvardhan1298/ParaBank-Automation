package com.fintech.tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.fintech.utils.DriverFactory;
import com.fintech.pages.LoginPage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.*;
import com.fintech.utils.ConfigReader;
import java.awt.Desktop;



import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import org.apache.commons.io.FileUtils;

public class BaseTest {

    protected WebDriver driver;
    protected static ExtentReports extent;
    protected ExtentTest test;
    protected static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private boolean loggedIn = false;
    
    protected WebDriverWait wait;

    @Parameters("browser")
    @BeforeClass(alwaysRun = true)
    public void setupClass(@Optional("chrome") String browserParam) {
    	String browser = ConfigReader.get("browser");

        driver = DriverFactory.getDriver(browser);

       
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(40));
    }

    @BeforeSuite(alwaysRun = true)
    public void initReport() {
        ExtentSparkReporter spark = new ExtentSparkReporter("target/ExtentReport.html");
        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Tester", "Vishnu");
        extent.setSystemInfo("Environment", "QA");
    }

    @BeforeMethod(alwaysRun = true)
    public void startTest(Method method) {
        test = extent.createTest(method.getDeclaringClass().getSimpleName() + "." + method.getName());
        test.assignCategory(ConfigReader.get("browser")); // Optional: tag by browser
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result) {
        if (result.getStatus() == ITestResult.SUCCESS) {
            test.pass("Test passed");
        } else if (result.getStatus() == ITestResult.FAILURE) {
            test.fail(result.getThrowable());
            takeScreenshot(result.getName());
        } else if (result.getStatus() == ITestResult.SKIP) {
            test.skip("Test skipped");
        }
    }

    private void takeScreenshot(String testName) {
        try {
            File screenshotsDir = new File("target/screenshots");
            if (!screenshotsDir.exists()) screenshotsDir.mkdirs();

            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            
            String fileName = testName + "_" + timestamp + ".png";
            File dest = new File(screenshotsDir, fileName);
            FileUtils.copyFile(screenshot, dest);
            test.addScreenCaptureFromPath(dest.getAbsolutePath());

        } catch (IOException | WebDriverException e) {
            if (test != null) test.warning("Screenshot failed: " + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        DriverFactory.quitDriver();
    }


    @AfterSuite(alwaysRun = true)
    public void finishReport() {
        if (extent != null) extent.flush();
        try {
            Desktop.getDesktop().browse(new File("target/ExtentReport.html").toURI());
        } catch (Exception ignored) {}
    }


    public void loginAsJohn() {
        if (loggedIn) return;

        driver.get(BASE_URL);
        System.out.println("Title: " + driver.getTitle());

        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();

        for (int i = 0; i < 2; i++) {
            loginPage.login("john", "demo");
            if (loginPage.isLoginSuccessful()) {
                loggedIn = true;
                return;
            }
            safeSleep(1000);
        }

        loginPage.dumpDebug();
        throw new AssertionError("Login failed — cannot proceed with test");
    }
    public void loginAs(String username, String password) {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        LoginPage login = new LoginPage(driver);
        login.enterUsername(username);
        login.enterPassword(password);
        login.clickLogin();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }



    protected WebDriver getDriver() {
        return this.driver;
    }

    protected void safeSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}



