package com.sprinklr.harvester.hotelsharvest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;

import com.sprinklr.harvester.model.InitialData;
import com.sprinklr.harvester.model.ReviewData;
import com.sprinklr.harvester.util.BaseDriverUtils;
import com.sprinklr.harvester.util.PropertyHandler;
import com.sprinklr.harvester.util.StaticUtils;

/**
 * Contain functions to fetch the data from the actual site -- Source CTRIP
 *
 */
public class HotelsExpectedDataFetch extends BaseDriverUtils {

	public static final Logger LOGGER = Logger.getLogger(HotelsExpectedDataFetch.class);
	private static final Date GIVENMENTIONDATE = StaticUtils.convertCtripStringToDate(PropertyHandler.getProperties()
	        .getProperty("date"));

	private static final String XPATH_CONTENT = PropertyHandler.getSourceProperties().getProperty("content_xpath");
	private static final String XPATH_MENTIONTIME = PropertyHandler.getSourceProperties().getProperty(
	        "mentionTime_xpath");
	private static final String XPATH_AUTHOR = PropertyHandler.getSourceProperties().getProperty("author_xpath");
	private static final String XPATH_RATING = PropertyHandler.getSourceProperties().getProperty("rating_xpath");

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
		return getDataFromHotelsSites(testData);
	}

	/**
	 * Get the expected data result set from the test site Hotels.
	 * 
	 * @param testData
	 *            - Test data hashmap containing the stub id and urls.
	 * 
	 * @return - Hashmapped test data for the expected results.
	 */
	public static HashMap<String, HashMap<String, ArrayList<ReviewData>>> getDataFromHotelsSites(
	        HashMap<Integer, InitialData> testData) {
		LOGGER.info("getDataFromHotelsSites - Inside get actual data method of class getDataFromHotelsSites...");

		// WebDriver driver = getDriver();
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

			NEXTSTUB: while (true) {
				List<WebElement> comments = getElementByXPath(XPATH_CONTENT);
				List<WebElement> mentionDates = getElementByXPath(XPATH_MENTIONTIME);
				List<WebElement> authorIDs = getElementByXPath(XPATH_AUTHOR);
				List<WebElement> ratings = getElementByXPath(XPATH_RATING);

				for (int i = 0; i < comments.size(); i++) {
					Date date = StaticUtils.convertHotelsStringToDate(mentionDates.get(i).getText());
					if (date.before(GIVENMENTIONDATE)) {
						break NEXTSTUB;
					}

					System.out.println("Commmmmmmm ==> " + comments.get(i).getText());
					System.out.println("Commmmmmmm ==> " + authorIDs.get(i).getText());
					System.out.println("Commmmmmmm ==> " + ratings.get(i).getText());
					commentsText.add(comments.get(i).getText());
					mentionDatesText.add(StaticUtils.convertDateToString(date));
					authorIDsText.add(authorIDs.get(i).getText());
					ratingsText.add(ratings.get(i).getText());
				}

				try { // move to next page
					endPointURL = endPointURL.replaceAll("page=" + pageNumber, "page=" + (pageNumber + 1));
					LOGGER.info("New endpoint is ==> " + endPointURL);
					pageNumber++;
					openBrowser(endPointURL); // load next page.
					continue;
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
		closeBrowser();
		return expectedReviewDataPerStub;
	}
}