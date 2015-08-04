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
 * Contain functions to fetch the data from the actual site -- Source - CTRIP
 *
 */
public class CtripExpectedDataFetch {

	public final static Logger LOGGER = Logger.getLogger(CtripExpectedDataFetch.class);
	private static WebDriver driver;
	private final static String BROWSER = PropertyHandler.getProperties().getProperty("browser");
	private static String givenDate = PropertyHandler.getProperties().getProperty("date");
	private static Date givenMentionDate = StaticUtils.convertCtripStringToDate(givenDate);

	public static WebDriver getHtmlDriver() {
		HtmlUnitDriver driver = new HtmlUnitDriver();
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(200, TimeUnit.SECONDS);
		driver.setJavascriptEnabled(true);
		return driver;
	}

	public static WebDriver getFirefoxDriver() {
		WebDriver driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(200, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.manage().deleteAllCookies();
		return driver;
	}

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
	 * 
	 * @param testData
	 * @return
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
				List<WebElement> comments = driver.findElements(By.xpath(PropertyHandler.getSourceProperties()
				        .getProperty("content_xpath")));
				List<WebElement> mentionDates = driver.findElements(By.xpath(PropertyHandler.getSourceProperties()
				        .getProperty("mentionTime_xpath")));
				List<WebElement> authorIDs = driver.findElements(By.xpath(PropertyHandler.getSourceProperties()
				        .getProperty("author_xpath")));
				List<WebElement> ratings = driver.findElements(By.xpath(PropertyHandler.getSourceProperties()
				        .getProperty("rating_xpath")));

				for (int i = 0; i < comments.size(); i++) {
					Date date = StaticUtils.convertCtripStringToDate(mentionDates.get(i).getText());
					if (date.before(givenMentionDate)) {
						break NEXTSTUB;
					}

					System.out.println("Author: " + authorIDs.get(i).getText() + " ===  Comments: "
					        + authorIDs.get(i).getText());
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
						openBrowser(endPointURL);
						continue;
					} else {
						WebElement button = (new WebDriverWait(driver, 200)).until(ExpectedConditions
						        .presenceOfElementLocated(By.xpath(next_page)));
						button.click();
						StaticUtils.pause(5);
						continue;
					}
				} catch (Exception e) {
					break;
				}
			}

			System.out.println("Comments Size: " + commentsText.size());
			System.out.println("Mention Time Size: " + mentionDatesText.size());
			System.out.println("Author Size: " + authorIDsText.size());
			System.out.println("Rating Size: " + ratingsText.size());

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
	 * 
	 * @param browser
	 * @param endPointURL
	 */
	public static void openBrowser(String endPointURL) {
		try {
			LOGGER.info("Trying to load the page. It will timeout in 200 secs. ==> " + endPointURL);
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
	 * 
	 */
	public static void selectDropdown() {
		String sort_dropdown = "//select[contains(@id,'selCommentSortType')] | //select[contains(@class,'select_sort')]";
		WebElement elem = (new WebDriverWait(driver, 200)).until(ExpectedConditions.presenceOfElementLocated(By
		        .xpath(sort_dropdown)));

		Select dropdown = new Select(elem);
		dropdown.selectByIndex(1);
		StaticUtils.pause(10);
	}
}