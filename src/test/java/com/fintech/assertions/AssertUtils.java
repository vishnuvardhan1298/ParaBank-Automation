package com.fintech.assertions;

import org.testng.Assert;

public class AssertUtils {

    public static void assertTransferSuccess(String confirmationText) {
        Assert.assertTrue(
            confirmationText.toLowerCase().contains("transfer complete"),
            "❌ Transfer confirmation missing or invalid"
        );
    }

    public static void assertBalanceUpdated(double before, double after, double expectedChange) {
        double delta = Math.abs((after - before) - expectedChange);
        Assert.assertTrue(delta < 0.01, "❌ Balance not updated correctly");
    }

    public static void assertErrorMessage(String actual, String expected) {
        Assert.assertTrue(actual.contains(expected), "❌ Expected error not shown");
    }
}
