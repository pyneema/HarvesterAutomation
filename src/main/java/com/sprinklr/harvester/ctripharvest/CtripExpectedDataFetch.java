package com.sprinklr.harvester.ctripharvest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.sprinklr.harvester.model.InitialData;
import com.sprinklr.harvester.model.ReviewData;
import com.sprinklr.harvester.util.PropertyHandler;
import com.sprinklr.harvester.util.StaticUtils;

/**
 * Contain functions to fetch the data from the actual site -- Source CTRIP
 *
 */
public class CtripExpectedDataFetch {

	public static final Logger LOGGER = Logger.getLogger(CtripExpectedDataFetch.class);
	private static final String BROWSER = PropertyHandler.getProperties().getProperty("browser");
	private static final Date GIVENMENTIONDATE = StaticUtils.convertCtripStringToDate(PropertyHandler.getProperties()
	        .getProperty("date"));

	private static final String XPATH_CONTENT = PropertyHandler.getSourceProperties().getProperty("content_xpath");
	private static final String XPATH_MENTIONTIME = PropertyHandler.getSourceProperties().getProperty(
	        "mentionTime_xpath");
	private static final String XPATH_AUTHOR = PropertyHandler.getSourceProperties().getProperty("author_xpath");
	private static final String XPATH_RATING = PropertyHandler.getSourceProperties().getProperty("rating_xpath");
	private static final String SORT_DROPDOWN = PropertyHandler.getSourceProperties().getProperty("sort_dropdown");
	private static WebDriver driver;

	/**
	 * Get the HTML driver instance.
	 * 
	 * @return - HTMLUNIT web driver.
	 */
	public static WebDriver getHtmlDriver() {
		HtmlUnitDriver driver = new HtmlUnitDriver();
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
		driver.setJavascriptEnabled(true);
		return driver;
	}

	/**
	 * Get the Firefox driver instance.
	 * 
	 * @return - Firefox web driver.
	 */
	public static WebDriver getFirefoxDriver() {
		WebDriver driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.manage().deleteAllCookies();
		return driver;
	}

	/**
	 * Get the expected data based on the browser property defined in
	 * Harvester.properties file.
	 * 
	 * @param testData
	 *            - Test data hashmap containing the stub id and urls.
	 * 
	 * @return - Hashmapped test data for the expected results.
	 */
	public static HashMap<String, HashMap<String, ArrayList<ReviewData>>> getExpectedData(
	        HashMap<Integer, InitialData> testData) {
		if (BROWSER.equalsIgnoreCase("firefox")) {
			driver = getFirefoxDriver();
			return getDataFromCtripSites(testData);
		} else if (BROWSER.equalsIgnoreCase("htmlunit")) {
			driver = getHtmlDriver();
			return getDataFromCtripSites(testData);
		} else {
			Assert.assertTrue(false, "No matching browser. Test Fail.");
		}
		return null;
	}

	/**
	 * Get the expected data result set from the test site CTRIP.
	 * 
	 * @param testData
	 *            - Test data hashmap containing the stub id and urls.
	 * 
	 * @return - Hashmapped test data for the expected results.
	 */
	public static HashMap<String, HashMap<String, ArrayList<ReviewData>>> getDataFromCtripSites(
	        HashMap<Integer, InitialData> testData) {
		LOGGER.info("CtripExpectedDataFetch - Inside get actual data method of class CtripExpectedDataFetch...");

		HashMap<String, HashMap<String, ArrayList<ReviewData>>> expectedReviewDataPerStub = new HashMap<String, HashMap<String, ArrayList<ReviewData>>>();

		Set<Integer> endPointKeySet = testData.keySet();
		Iterator<Integer> endPointIterator = endPointKeySet.iterator();
		while (endPointIterator.hasNext()) {
			ArrayList<String> commentsText = new ArrayList<String>();
			ArrayList<String> mentionDatesText = new ArrayList<String>();
			ArrayList<String> authorIDsText = new ArrayList<String>();
			ArrayList<String> ratingsText = new ArrayList<String>();

			HashMap<String, ArrayList<ReviewData>> reviewContent = new HashMap<String, ArrayList<ReviewData>>();

			Integer stubID = endPointIterator.next();
			String endPointURL = testData.get(stubID).getStubEndpoint();
			System.out.println("==> Endpoint " + endPointURL);
			int pageNumber = 1;

			openBrowser(endPointURL);

			selectDropdown();

			NEXTSTUB: while (true) {
				List<WebElement> comments = driver.findElements(By.xpath(XPATH_CONTENT));
				List<WebElement> mentionDates = driver.findElements(By.xpath(XPATH_MENTIONTIME));
				List<WebElement> authorIDs = driver.findElements(By.xpath(XPATH_AUTHOR));
				List<WebElement> ratings = driver.findElements(By.xpath(XPATH_RATING));

				for (int i = 0; i < comments.size(); i++) {
					Date date = StaticUtils.convertCtripStringToDate(mentionDates.get(i).getText());
					if (date.before(GIVENMENTIONDATE)) {
						break NEXTSTUB;
					}
					commentsText.add(comments.get(i).getText());
					mentionDatesText.add(mentionDates.get(i).getText());
					authorIDsText.add(authorIDs.get(i).getText());
					ratingsText.add(ratings.get(i).getText());
				}

				try { // move to next page
					String next_page = "//a[contains(@class,'c_down')]";
					if (endPointURL.toLowerCase().contains("ajax")) {
						endPointURL = endPointURL.replaceAll("currentPage=" + pageNumber, "currentPage="
						        + (pageNumber + 1));
						LOGGER.info("New endpoint is ==> " + endPointURL);
						pageNumber++;
						openBrowser(endPointURL); // load next page.
						continue;
					} else {
						WebElement button = (new WebDriverWait(driver, 200)).until(ExpectedConditions
						        .presenceOfElementLocated(By.xpath(next_page)));
						button.click(); // click on next button.
						StaticUtils.pause(5);
						continue;
					}
				} catch (Exception e) {
					break;
				}
			}

			LOGGER.info("Comments Size: " + commentsText.size());
			LOGGER.info("Mention Time Size: " + mentionDatesText.size());
			LOGGER.info("Author Size: " + authorIDsText.size());
			LOGGER.info("Rating Size: " + ratingsText.size());

			for (int i = 0; i < commentsText.size(); i++) {
				ReviewData rdObject = new ReviewData();
				rdObject.setHarvesterID(stubID.toString());
				rdObject.setAuthorId(authorIDsText.get(i));
				rdObject.setComment(commentsText.get(i).replaceAll("  +", " "));
				rdObject.setMentionedDate(mentionDatesText.get(i));
				rdObject.setRatings(ratingsText.get(i));

				Set<String> reviewContentKeyset = reviewContent.keySet();
				if (reviewContentKeyset.contains(rdObject.getAuthorId())) {
					ArrayList<ReviewData> reviewData = new ArrayList<ReviewData>();
					reviewData = reviewContent.get(rdObject.getAuthorId());
					reviewData.add(rdObject);
					reviewContent.put(rdObject.getAuthorId(), reviewData);
				} else {
					ArrayList<ReviewData> reviewData = new ArrayList<ReviewData>();
					reviewData.add(rdObject);
					reviewContent.put(rdObject.getAuthorId(), reviewData);
				}
			}
			expectedReviewDataPerStub.put(stubID.toString(), reviewContent);
		}
		return expectedReviewDataPerStub;
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
		} catch (Exception e) {
			StaticUtils.pause(5);
			if (BROWSER.equalsIgnoreCase("firefox")) {
				JavascriptExecutor js = (JavascriptExecutor) driver;
				js.executeScript("return window.stop()");
			}
		}
	}

	/**
	 * Sort the reviews in latest date order.
	 */
	public static void selectDropdown() {
		WebElement selectDropdown = (new WebDriverWait(driver, 200)).until(ExpectedConditions
		        .presenceOfElementLocated(By.xpath(SORT_DROPDOWN)));
		Select dropdown = new Select(selectDropdown);
		dropdown.selectByIndex(1);
		StaticUtils.pause(10);
	}
}