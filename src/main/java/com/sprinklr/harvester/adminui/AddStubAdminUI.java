package com.sprinklr.harvester.adminui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import com.sprinklr.harvester.util.JdbcConnect;
import com.sprinklr.harvester.util.PropertyHandler;
import com.sprinklr.harvester.util.StaticUtils;

/**
 * Add all the stubs in DB from the Admin UI.
 *
 */
public class AddStubAdminUI {
	public final static Logger LOGGER = Logger.getLogger(AddStubAdminUI.class);

	public void addStubInAdminUI() {

		LOGGER.info("AddStubAdminUI.addStubInAdminUI() Initialising the FirefoxDriver for adding new stub...");

		ArrayList<String> stubUrl = new ArrayList<String>();
		List<String> urlListToInsert = getUrlList();
		LOGGER.info("AddStubAdminUI.addStubInAdminUI() getting list of stubs/urls to add, from csv file");
		Iterator<String> urlListIterator = urlListToInsert.iterator();

		while (urlListIterator.hasNext()) {
			String nextURL = urlListIterator.next();
			if (JdbcConnect.getStubID(nextURL) != 0) {
				LOGGER.warn("AddStubAdminUI.addStubInAdminUI() stub already exists in DB! " + nextURL);
				continue;
			}
			stubUrl.add(nextURL);
		}

		if (stubUrl.size() <= 0) {
			return;
		}

		WebDriver driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(40, TimeUnit.SECONDS);
		driver.manage().window().maximize();

		String url = PropertyHandler.getProperties().getProperty("adminURL");
		String username = PropertyHandler.getProperties().getProperty("adminuser");
		String password = PropertyHandler.getProperties().getProperty("adminpassword");
		LOGGER.info("AddStubAdminUI.addStubInAdminUI() Admin url to open : " + url);
		String client = PropertyHandler.getProperties().getProperty("client");
		LOGGER.info(" AddStubAdminUI.addStubInAdminUI() Client : " + client);

		driver.get(url + "/" + client + "/login.jsp");
		LOGGER.info("AddStubAdminUI.addStubInAdminUI() opening page : " + url + "/" + client + "/login.jsp");

		driver.findElement(By.name("uname")).sendKeys(username);
		LOGGER.info("AddStubAdminUI.addStubInAdminUI() Passing username : " + username);
		driver.findElement(By.name("passcode")).sendKeys(password);
		LOGGER.info("AddStubAdminUI.addStubInAdminUI() Passing password : " + password);
		driver.findElement(By.name("submit")).click();

		for (String nextURL : stubUrl) {
			LOGGER.info("AddStubAdminUI.addStubInAdminUI() checking for stubs existance : " + nextURL);

			driver.findElement(By.id("extdd-18")).click();
			driver.switchTo().frame(0);
			clickManageDropDownBox(driver);
			driver.findElement(By.id("ext-gen57")).click();

			driver.switchTo().defaultContent();
			driver.switchTo().frame(1);
			driver.findElement(By.name("url")).sendKeys(nextURL);

			Select select = new Select(driver.findElement(By.name("source_id")));
			select.selectByVisibleText(PropertyHandler.getProperties().getProperty("source"));

			Select selectOrg = new Select(driver.findElement(By.name("org_id")));
			selectOrg.selectByVisibleText(PropertyHandler.getProperties().getProperty("location"));

			driver.findElement(By.name("Submit")).click();
			StaticUtils.pause(5);
			driver.switchTo().defaultContent();
		}
		LOGGER.info("AddStubAdminUI.addStubInAdminUI() closing FirefoxDriver");
		driver.close();
	}

	/**
	 * Method to fetch list of stub URL from CSV file
	 * 
	 * @return List<String> list of stub URL from CSV file
	 */
	public static List<String> getUrlList() {
		LOGGER.info("AddStubAdminUI.getUrlList() returning list of Urls from csv file");
		return JdbcConnect.csvParser();
	}

	/**
	 * Method to click Manage Drop Down Box
	 */
	public void clickManageDropDownBox(WebDriver driver) {
		LOGGER.info("AddStubAdminUI.clickManageDropDownBox() clicking manage drop down box");
		Actions builder1 = new Actions(driver);
		WebElement e = driver.findElement(By.xpath("//em[@class='x-btn-split']"));
		builder1.moveToElement(e);

		int h = e.getSize().getHeight();
		int w = e.getSize().getWidth();

		Actions builder2 = new Actions(driver);
		builder2.moveToElement(e, w - 5, h).click().build().perform();
		StaticUtils.pause(2);
	}
}