package com.sprinklr.harvester.util;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.testng.Assert;

public class BaseDriverUtils {

	public static final Logger LOGGER = Logger.getLogger(BaseDriverUtils.class);
	private static final String BROWSER = PropertyHandler.getProperties().getProperty("browser");
	private static WebDriver driver;

	public BaseDriverUtils() {
		System.out.println("=== In constructor Base utiotls => " + getCurrentPath().toString());
		System.setProperty("log.path", getCurrentPath());
		if (BROWSER.equalsIgnoreCase("firefox")) {
			driver = getFirefoxDriver();
		} else if (BROWSER.equalsIgnoreCase("htmlunit")) {
			driver = getHtmlDriver();
		} else if (BROWSER.equalsIgnoreCase("phantomjs")) {
			driver = getPhatomJSDriver();
		} else {
			Assert.assertTrue(false, "No matching browser. Test Fail.");
		}
	}

	public static WebDriver getDriver() {
		return driver;
	}

	/**
	 * Get the HTML driver instance.
	 * 
	 * @return - HTMLUNIT web driver.
	 */
	public static WebDriver getHtmlDriver() {
		driver = new HtmlUnitDriver();
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
		((HtmlUnitDriver) driver).setJavascriptEnabled(true);
		return driver;
	}

	/**
	 * Get the PhatomJS driver instance.
	 * 
	 * @return - PhantomJS web driver.
	 */
	public static WebDriver getPhatomJSDriver() {
		driver = new PhantomJSDriver();
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
		driver.manage().deleteAllCookies();
		return driver;
	}

	/**
	 * Get the Firefox driver instance.
	 * 
	 * @return - Firefox web driver.
	 */
	public static WebDriver getFirefoxDriver() {
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.manage().deleteAllCookies();
		return driver;
	}

	/**
	 * Open the specified URL in given browser.
	 * 
	 * @param endPointURL
	 */
	public static void openBrowser(String endPointURL) {
		try {
			LOGGER.info("Trying to load the page ==> " + endPointURL);
			driver.navigate().to(endPointURL);
			if (BROWSER.equalsIgnoreCase("htmlunit")) {
				StaticUtils.pause(30);
			}
		} catch (Exception e) {
			StaticUtils.pause(5);
			if (BROWSER.equalsIgnoreCase("firefox") || BROWSER.equalsIgnoreCase("phatomjs")) {
				JavascriptExecutor js = (JavascriptExecutor) driver;
				js.executeScript("return window.stop()");
			} else if (BROWSER.equalsIgnoreCase("htmlunit")) {
				driver.manage().timeouts().pageLoadTimeout(200, TimeUnit.SECONDS);
				StaticUtils.pause(20);
			}
		}
	}

	static String getCurrentPath() {
		return System.getProperty("user.dir") + "/logs";
	}

	public static void closeBrowser() {
		driver.close();
	}

	public static List<WebElement> getElementByXPath(String xpath) {
		try {
			return driver.findElements(By.xpath(xpath));
		} catch (Exception ex) {
			return null;
		}
	}
}
