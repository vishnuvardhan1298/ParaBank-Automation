package com.fintech.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class NavigationPage extends BasePage {

    private By homeLink = By.xpath("//a[contains(@href,'index.htm') or contains(@class,'brand') or contains(@class,'logo')]");
    private By accountsLink = By.linkText("Accounts Overview");
    private By transferLink = By.linkText("Transfer Funds");
    private By loanLink = By.linkText("Request Loan");
    private By contactLink = By.linkText("Contact Us");
    private By logo = By.cssSelector("div#headerPanel a[href*='index.htm']");

    public NavigationPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isLoaded() {
        return isDisplayed(homeLink) || isDisplayed(logo);
    }

    public NavigationPage goHome() {
        click(homeLink);
        return this;
    }

    public NavigationPage goToAccounts() {
        click(accountsLink);
        return this;
    }

    public NavigationPage goToTransfer() {
        click(transferLink);
        return this;
    }

    public NavigationPage goToLoan() {
        click(loanLink);
        return this;
    }

    public NavigationPage goToContact() {
        click(contactLink);
        return this;
    }

    public NavigationPage clickLogo() {
        waitForClickable(logo);
        click(logo);
        return this;
    }

    public NavigationPage clickNavLink(String linkText) {
        By navLink = By.xpath("//a[contains(text(),'" + linkText + "')]");
        safeSleep(1000); // Stabilize DOM
        waitForClickable(navLink);
        click(navLink);
        return this;
    }
}

