package com.fintech.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public final class ReportManager {
  private static final ExtentReports extent = new ExtentReports();
  private static final ExtentSparkReporter spark = new ExtentSparkReporter("test-output/ExtentReport.html");

  static {
    extent.attachReporter(spark);
  }

  private ReportManager() {}

  public static ExtentTest createTest(String name) {
    return extent.createTest(name);
  }

  public static void flush() {
    extent.flush();
  }
}

