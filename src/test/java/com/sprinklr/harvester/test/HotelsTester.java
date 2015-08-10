package com.sprinklr.harvester.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sprinklr.harvester.adminui.AddStubAdminUI;
import com.sprinklr.harvester.global.CompareFunctions;
import com.sprinklr.harvester.hotelsharvest.HotelsExpectedDataFetch;
import com.sprinklr.harvester.model.InitialData;
import com.sprinklr.harvester.model.ReviewData;
import com.sprinklr.harvester.mq.RabbitMQFunctions;
import com.sprinklr.harvester.mq.RabbitMQPullMessage;
import com.sprinklr.harvester.mq.RabbitMQPushMessage;
import com.sprinklr.harvester.util.BaseDriverUtils;
import com.sprinklr.harvester.util.JdbcConnect;
import com.sprinklr.harvester.util.PropertyHandler;

/**
 * Test the CTrip harvester.
 *
 */
public class HotelsTester extends BaseDriverUtils {

	public static final Logger LOGGER = Logger.getLogger(HotelsTester.class);

	/**
	 * Add all of the stubs that does not exists in DB. Add it through ADMIN UI.
	 */
	// @BeforeClass
	public void addSutbsInDB() {
		LOGGER.info("addSutbsInDB() initialising the testing process...");
		AddStubAdminUI addStub = new AddStubAdminUI();
		addStub.addStubInAdminUI();
		LOGGER.info("addSutbsInDB() completed adding stub/url from adminUI");
	}

	/**
	 * Purge the push and pull queues.
	 */
	@BeforeMethod
	public void cleanQueues() {
		LOGGER.info("Purging the queues..");
		RabbitMQFunctions.purgeQueue(PropertyHandler.getProperties().getProperty("push_queue"));
		RabbitMQFunctions.purgeQueue(PropertyHandler.getProperties().getProperty("pull_queue"));
	}

	/**
	 * CTrip Harvester tests.
	 * 
	 * @param url
	 * @param canonicalUrl
	 */
	@Test(dataProvider = "hotelsdata")
	public void verifyHotelsHarvester(String url, String canonicalUrl) {
		LOGGER.info("verifyHotelsHarvester() Call create csv to fetch the stub ids from db.");
		HashMap<Integer, InitialData> testData = JdbcConnect.getHashMappedDBData(url, canonicalUrl);
		LOGGER.info("verifyHotelsHarvester() completed fetching data from database & convert into HashMap<Integer, InitialData> testData ");

/*		LOGGER.info("verifyHotelsHarvester() starting to push the stubs in RabbitMQ");
		RabbitMQPushMessage.push(testData);
		LOGGER.info("verifyHotelsHarvester() completed pushing all the stubUrls to queue from testData");

		LOGGER.info("verifyHotelsHarvester() start pulling messages from MQ for provided stubs");
		HashMap<String, HashMap<String, ArrayList<ReviewData>>> actualData = RabbitMQPullMessage.pull();
		LOGGER.info("verifyHotelsHarvester() completed pulling messages from MQ for provided stubs");*/

		LOGGER.info("verifyHotelsHarvester() starting fetching data from actual webpage");
		HashMap<String, HashMap<String, ArrayList<ReviewData>>> expectedData = HotelsExpectedDataFetch
		        .getExpectedData(testData);
		LOGGER.info("verifyHotelsHarvester() completed fetching data from actual webpage");

		LOGGER.info("verifyHotelsHarvester() starting comparison of expected data with actual data");
		// CompareFunctions.compareData(actualData, expectedData);
	}

	/**
	 * CTrip test data provider.
	 * 
	 * @return - 2 Dimensional array of CSV file.
	 */
	@DataProvider(name = "hotelsdata")
	public Object[][] HotelsDataTest() {
		String source = PropertyHandler.getProperties().getProperty("source").toLowerCase();
		String csvFileToParse = System.getProperty("user.dir") + "\\src\\main\\resources\\" + source + "\\" + source
		        + ".csv";
		Object[][] arrayObject = getCSVData(csvFileToParse);
		return arrayObject;
	}

	/**
	 * Parse the input CSV file and the function will return 2 dimensional array
	 * of the CSV file.
	 * 
	 * @param fileName
	 * @return
	 */
	public String[][] getCSVData(String fileName) {
		String[][] arrayS = null;
		BufferedReader bufferedReader = null;
		BufferedReader bufferedReader2 = null;
		String line = "";

		try {
			bufferedReader2 = new BufferedReader(new FileReader(fileName));
			int countRow = 0;
			while (bufferedReader2.readLine() != null) {
				countRow++;
			}
			arrayS = new String[countRow][2];

			bufferedReader = new BufferedReader(new FileReader(fileName));
			int i = 0;
			while ((line = bufferedReader.readLine()) != null) {
				arrayS[i][0] = line.split(",")[0];
				arrayS[i][1] = line.split(",")[1];
				i++;
			}
		} catch (FileNotFoundException fne) {
			fne.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (bufferedReader != null && bufferedReader2 != null) {
				try {
					bufferedReader.close();
					bufferedReader2.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		return arrayS;
	}

}
