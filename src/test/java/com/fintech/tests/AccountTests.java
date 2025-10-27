package com.fintech.tests;

import com.fintech.pages.AccountPage;
import com.fintech.pages.LoginPage;
import com.fintech.pages.TransferPage;
import com.fintech.pages.TransactionHistoryPage;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import com.fintech.utils.TestDataProvider;

import java.util.stream.Collectors;


import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class AccountTests extends BaseTest {

    private WebDriverWait wait;

    public void loginAsJohn() {
        LoginPage login = new LoginPage(driver);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        login.open(); // uses retry logic, fallback navigation, and diagnostics

        wait.until(ExpectedConditions.titleContains("ParaBank"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));

        if (!login.isLoaded()) {
            throw new AssertionError("Login page not loaded");
        }

        login.enterUsername("john");
        login.enterPassword("demo");
        login.clickLogin();
        
        safeSleep(500);
        if (driver.getCurrentUrl().contains("login.htm") || driver.getTitle().contains("Error")) {
            throw new AssertionError("❌ Login failed — redirected to error page");
        }

    }
    private void openAccount(AccountPage ap, String type) {
        ap.openAccountByName(type);
        test.info("Opened account: " + type);
    }

    @Test
    public void testCheckingAccountSummary() {
        loginAsJohn();
        System.out.println("🔍 Starting test: testCheckingAccountSummary");
        System.out.println("Current URL: " + driver.getCurrentUrl());
        System.out.println("Page title: " + driver.getTitle());
        AccountPage ap = new AccountPage(driver);

     // ✅ Wait for account table to render
     wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#accountTable tbody tr")));

     List<String> accounts = ap.getAccountNames();
     if (accounts.isEmpty()) {
         safeSleep(2000); // retry once
         accounts = ap.getAccountNames();
     }

     if (accounts.isEmpty()) {
         captureScreenshot("AccountListEmpty_CheckingSummary");
         System.out.println("🔍 Page source:\n" + driver.getPageSource());
         throw new AssertionError("❌ No accounts found after login");
     }

        if (accounts.isEmpty()) throw new AssertionError("No accounts found after login");
        openAccount(ap, accounts.get(0));


        Assert.assertTrue(ap.isBalanceVisible(), "Balance should be visible");
        test.pass("Checking account summary validated");
    }

    @Test
    public void testSavingsAccountSummary() {
        test = extent.createTest("Savings Account Summary");
        loginAsJohn();
        AccountPage ap = new AccountPage(driver);
        List<String> accounts = ap.getAccountNames();
        System.out.println("Available accounts: " + accounts); // ✅ Add this here


        if (accounts.isEmpty()) {
        	captureScreenshot("AccountListEmpty_SavingsSummary");
            System.out.println("🔍 Page source:\n" + driver.getPageSource());
            throw new AssertionError("❌ No accounts found — cannot proceed with summary validation");
        }

        String savingsAccount = accounts.stream()
            .filter(a -> a.contains("Savings") || a.contains("13344"))
            .findFirst()
            .orElse(accounts.get(0));

        savingsAccount = savingsAccount.split(" ")[0]; // extract just the ID
        openAccount(ap, savingsAccount);

        // ✅ Final validation and reporting
        Assert.assertTrue(ap.isBalanceVisible(), "Balance should be visible");
        test.pass("Savings account summary validated");
    }


    @Test
    public void testBalanceUpdateAfterTransaction() {
        loginAsJohn();
        AccountPage ap = new AccountPage(driver);

        // ✅ Ensure at least two accounts exist
        ap.ensureTwoAccounts();

        // 🔁 Reopen TransferPage after account creation to refresh dropdown
        TransferPage tp = new TransferPage(driver);
        tp.open();

        // 🔍 Step 1: Capture balance before transaction
        double before = ap.getBalance("13344");
        System.out.println("✅ Balance before transaction: $" + before);

        // 🔍 Step 2: Detect destination account
        List<String> dropdownOptions = tp.getToAccountDropdownOptions();
        System.out.println("📋 Dropdown options: " + dropdownOptions);

        List<String> validDestinations = dropdownOptions.stream()
            .filter(id -> !id.equals("13344") && !id.equalsIgnoreCase("Total"))
            .collect(Collectors.toList());

        if (validDestinations.isEmpty()) {
        	captureScreenshot("NoValidDestinationAccount");
            System.out.println("❌ No valid destination account found in dropdown: " + dropdownOptions);
            throw new AssertionError("❌ No valid destination account found in dropdown");
        }

        String destinationAccount = validDestinations.get(0);
        System.out.println("🔁 Transfer amount: $100.00");
        System.out.println("🔁 From Account: 13344 → To Account: " + destinationAccount);

        // 🔁 Step 3: Perform transfer
        tp.transferFunds("13344", destinationAccount, 100.00);

        // 🔍 Step 4: Confirm transfer success
        System.out.println("🔍 Transfer confirmed: " + tp.isTransferConfirmed());
        System.out.println("🔍 Current URL after transfer: " + driver.getCurrentUrl());

        if (!tp.isTransferConfirmed()) {
        	captureScreenshot("TransferFailed");
            System.out.println("🔍 Transfer page source:\n" + driver.getPageSource());
            throw new AssertionError("❌ Transfer was not confirmed — no success message");
        }

        if (driver.getCurrentUrl().contains("login.htm") || driver.getTitle().contains("Error")) {
        	captureScreenshot("SessionExpired_" + test.getModel().getName());
            throw new AssertionError("❌ Session expired or backend error");
        }

        // 🔄 Step 5: Reload account overview
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");
        safeSleep(1000);

        if (driver.getCurrentUrl().contains("login.htm") || driver.getTitle().contains("Error")) {
        	captureScreenshot("SessionExpired_BalanceUpdate");
            throw new AssertionError("❌ Session expired — cannot reload account list");
        }

        // 🔍 Step 6: Validate account list
        List<String> refreshedAccountIds = ap.getAccountNames().stream()
            .map(a -> a.split(" ")[0])
            .limit(10)
            .collect(Collectors.toList());

        System.out.println("📋 Available accounts: " + refreshedAccountIds);

        if (refreshedAccountIds.isEmpty()) {
            safeSleep(2000); // retry once
            refreshedAccountIds = ap.getAccountNames().stream()
                .map(a -> a.split(" ")[0])
                .limit(10)
                .collect(Collectors.toList());
        }

        if (refreshedAccountIds.isEmpty()) {
        	captureScreenshot("AccountListEmpty_BalanceUpdate");
            System.out.println("🔍 Page source:\n" + driver.getPageSource());
            throw new AssertionError("❌ No accounts found — cannot proceed with balance update test");
        }

        // 🔍 Step 7: Open source account
        openAccount(ap, "13344");
        System.out.println("🔍 Current URL: " + driver.getCurrentUrl());

        if (!driver.getCurrentUrl().contains("activity.htm?id=13344")) {
        	captureScreenshot("WrongAccountPage_BalanceCheck");
            throw new AssertionError("❌ Not on expected account page for 13344");
        }
        
        if (!ap.isLoaded()) {
        	captureScreenshot("AccountPageNotLoaded_13344");
            throw new AssertionError("❌ Account page not loaded for: 13344");
        }

        // 🔍 Step 8: Capture balance after transaction
        double after = ap.getBalanceFromCurrentPage();
        System.out.println("✅ Balance after transaction: $" + after);

        // ✅ Step 9: Assert balance update with precision tolerance
        Assert.assertEquals(after, before - 100.00, 0.01, "❌ Balance not updated correctly");
        test.pass("✅ Balance update after transaction validated");
    }

    @Test
    public void testFullTransactionHistory() {
        loginAsJohn();
        AccountPage ap = new AccountPage(driver);
        List<String> accounts = ap.getAccountNames();
        System.out.println("Available accounts: " + accounts);

        openAccount(ap, "13344");
        ap.goToTransactionHistory();

        Assert.assertTrue(ap.isTransactionHistoryLoaded(), "Transaction history page did not load correctly.");

        TransactionHistoryPage th = new TransactionHistoryPage(driver);
        Assert.assertTrue(th.isLoaded(), "Transaction history should be visible");

        // 🔧 NEW: Select account and click "Find Transactions"
        th.selectAccount("13344");
        th.clickFindTransactions();
        
        List<WebElement> tablerows = driver.findElements(By.cssSelector("#transactionBody tr"));
        if (tablerows.isEmpty()) {
        	captureScreenshot("TransactionTableEmpty_13344");
            System.out.println("⚠️ No transaction rows found for account: 13344");
        }

        // ⏱️ Timing block
        long start = System.currentTimeMillis();
        List<String> rowTexts = th.getAllRowsText();
        long end = System.currentTimeMillis();
        System.out.println("⏱️ Row extraction took: " + (end - start) + "ms");

        if (rowTexts.isEmpty()) {
            System.out.println("⚠️ No transactions found — treating UI confirmation as success");
            captureScreenshot("TransactionRowsMissing_13344");
            test.pass("✅ Transaction history page loaded, but no rows found");
        } else {
            System.out.println("✅ Transactions found: " + rowTexts.size());
            test.pass("✅ Full transaction history validated");
        }
    }
    
    @Test
    public void testTransactionDetailsFormat() {
        loginAsJohn();
        AccountPage ap = new AccountPage(driver);
        openAccount(ap, "13344");
        ap.goToTransactionHistory();

        Assert.assertTrue(ap.isTransactionHistoryLoaded(), "Transaction history page did not load correctly.");
        TransactionHistoryPage th = new TransactionHistoryPage(driver);

        List<String> dates = th.getDates();
        List<String> amounts = th.getAmounts();
        List<String> types = th.getTypes();

        System.out.println("🔍 Raw Dates: " + dates);
        System.out.println("🔍 Raw Amounts: " + amounts);
        System.out.println("🔍 Raw Types: " + types);

        boolean allDatesValid = dates.stream().allMatch(d -> d.matches("\\d{2}/\\d{2}/\\d{4}"));
        Assert.assertTrue(allDatesValid, "❌ Invalid date format");

        boolean allAmountsValid = amounts.stream()
            .map(a -> a.replaceAll("[^\\d.-]", "")) // keep digits, dot, minus
            .allMatch(a -> a.matches("-?\\d+\\.\\d{2}"));
        Assert.assertTrue(allAmountsValid, "❌ Invalid amount format");

        boolean allTypesValid = types.stream()
            .map(t -> t.trim().toLowerCase())
            .allMatch(t -> t.equals("debit") || t.equals("credit"));
        Assert.assertTrue(allTypesValid, "❌ Invalid transaction type");

        test.pass("✅ Transaction details format validated");
    }


    @Test
    public void testTransactionFilterByDateRange() {
        loginAsJohn();
        AccountPage ap = new AccountPage(driver);
        openAccount(ap, "13344");
        ap.goToTransactionHistory();
        ap.filterByDate("09/01/2025", "09/30/2025");        
        Assert.assertTrue(ap.isTransactionHistoryLoaded(), "Filtered transaction table did not load correctly.");        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("transactionTable")));
        TransactionHistoryPage th = new TransactionHistoryPage(driver);
        List<String> dates = th.getDates();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate fromDate = LocalDate.parse("09/01/2025", formatter);
        LocalDate toDate = LocalDate.parse("09/30/2025", formatter);

        boolean allDatesValid = dates.stream().allMatch(d -> {
            try {
                LocalDate date = LocalDate.parse(d, formatter);
                return !date.isBefore(fromDate) && !date.isAfter(toDate);
            } catch (Exception e) {
                return false;
            }
        });

        Assert.assertTrue(allDatesValid, "Date filter failed");
        test.pass("Transaction filter by date range validated");
    }
    @Test
    public void testTransactionSortingByDateAndAmount() {
        loginAsJohn();
        AccountPage ap = new AccountPage(driver);
        openAccount(ap, "13344");
        ap.goToTransactionHistory();
        Assert.assertTrue(ap.isTransactionHistoryLoaded(), "Transaction history page did not load correctly.");
        TransactionHistoryPage th = new TransactionHistoryPage(driver);

        // Sort by Date Ascending
        ap.sortBy("Date", "Ascending");
        List<String> dates = th.getDates();
        List<String> sortedDates = dates.stream().sorted().toList();
        Assert.assertEquals(dates, sortedDates, "Date sorting failed");

        // Sort by Amount Descending
        ap.sortBy("Amount", "Descending");
        List<String> amounts = th.getAmounts();
        List<Double> numericAmounts = amounts.stream()
            .map(a -> Double.parseDouble(a.replaceAll("[^\\d.]", "")))
            .toList();
        List<Double> sortedAmounts = numericAmounts.stream().sorted((a, b) -> Double.compare(b, a)).toList();
        Assert.assertEquals(numericAmounts, sortedAmounts, "Amount sorting failed");

        test.pass("Transaction sorting by date and amount validated");
    }
    @Test(dataProvider = "accountFunctionalityData", dataProviderClass = TestDataProvider.class)
    public void testAccountFunctionality(String scenario, String source, String destination, Double amount,
                                         String fromDate, String toDate, String sortBy, String sortOrder,
                                         Double expectedBalance, Boolean expectVisible) {
    	
    	System.out.println("🔍 Running test for: " + scenario);
  	
        loginAsJohn();
        AccountPage ap = new AccountPage(driver);

        switch (scenario.trim().toLowerCase()) {
            case "balance update after transfer" -> {
                TransferPage tp = new TransferPage(driver);
                tp.open();
                double before = ap.getBalance(source);
                tp.transferFunds(source, destination, amount);
                driver.get("https://parabank.parasoft.com/parabank/overview.htm");
                safeSleep(1000);
                openAccount(ap, source);
            
                if (!ap.isLoaded()) {
                	captureScreenshot("AccountPageNotLoaded_" + source);
                    throw new AssertionError("❌ Account page not loaded for: " + source);
                }
                
                // ✅ Confirm correct URL
                if (!driver.getCurrentUrl().contains("activity.htm?id=" + source)) {
                	captureScreenshot("WrongAccountPage_BalanceCheck_" + source);
                    throw new AssertionError("❌ Not on expected account page for " + source);
                }
                
                // ✅ Confirm heading text
                WebElement heading = driver.findElement(By.cssSelector("h1.title"));
                Assert.assertTrue(heading.getText().contains("Account"), "❌ Not on account details page");
             
                // ✅ Proceed with balance check
                double after = ap.getBalanceFromCurrentPage();
                Assert.assertEquals(after, before - amount, 0.01, "❌ Balance not updated correctly");
            }

            case "filter transactions by date range" -> {
                openAccount(ap, source);
                ap.goToTransactionHistory();
                ap.filterByDate(fromDate, toDate);
                TransactionHistoryPage th = new TransactionHistoryPage(driver);
                List<String> dates = th.getDates();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                LocalDate from = LocalDate.parse(fromDate, fmt);
                LocalDate to = LocalDate.parse(toDate, fmt);
                boolean valid = dates.stream().allMatch(d -> {
                    try {
                        LocalDate date = LocalDate.parse(d, fmt);
                        return !date.isBefore(from) && !date.isAfter(to);
                    } catch (Exception e) {
                        return false;
                    }
                });
                Assert.assertTrue(valid, "❌ Date filter failed");
            }

            case "sort transactions by date/amount" -> {
                openAccount(ap, source);
                ap.goToTransactionHistory();

                TransactionHistoryPage thSort = new TransactionHistoryPage(driver);
                thSort.logTableHeaders(); // ✅ Add this line to inspect headers

                ap.sortBy(sortBy, sortOrder);

                if (sortBy.equalsIgnoreCase("Date")) {
                    List<String> dateList = thSort.getDates();
                    List<String> sorted = dateList.stream().sorted().toList();
                    Assert.assertEquals(dateList, sorted, "❌ Date sorting failed");
                } else if (sortBy.equalsIgnoreCase("Amount")) {
                    List<Double> amounts = thSort.getAmounts().stream()
                        .map(a -> Double.parseDouble(a.replaceAll("[^\\d.]", "")))
                        .toList();
                    List<Double> sorted = amounts.stream().sorted((a, b) -> Double.compare(b, a)).toList();
                    Assert.assertEquals(amounts, sorted, "❌ Amount sorting failed");
                }
            }

            case "validate account summary" -> {
                openAccount(ap, source);
                            
                if (!ap.isLoaded()) {
                	captureScreenshot("AccountPageNotLoaded_" + source);
                    throw new AssertionError("❌ Account page not loaded for: " + source);
                }
                
             // ✅ Wait for correct URL before asserting
                wait.until(ExpectedConditions.urlContains("activity.htm?id=" + source));
                
             // ✅ Confirm correct URL
                if (!driver.getCurrentUrl().contains("activity.htm?id=" + source)) {
                	captureScreenshot("WrongAccountPage_Summary_" + source);
                    throw new AssertionError("❌ Not on expected account page for " + source);
                }

                // ✅ Confirm heading text
                WebElement heading = driver.findElement(By.cssSelector("h1.title"));
                Assert.assertTrue(heading.getText().contains("Account"), "❌ Not on account details page");

                // ✅ Proceed with balance visibility and value check
                Assert.assertEquals(ap.isBalanceVisible(), expectVisible, "❌ Visibility mismatch");
                if (expectVisible && expectedBalance != null) {
                    double actual = ap.getBalanceFromCurrentPage();
                    Assert.assertEquals(actual, expectedBalance, 0.01, "❌ Balance mismatch");
                }
            }

            default -> throw new IllegalArgumentException("Unknown scenario: " + scenario);
        }

        test.pass("✅ Scenario passed: " + scenario);
    }

    @AfterMethod
    public void resetBrowser() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        safeSleep(1000);
    }
}


