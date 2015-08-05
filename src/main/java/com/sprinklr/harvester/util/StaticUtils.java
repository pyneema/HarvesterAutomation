package com.sprinklr.harvester.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * General reusable functions.
 */
public class StaticUtils {
	
	public static final Logger LOGGER = Logger.getLogger(StaticUtils.class);

	/**
	 * Sleep for seconds.
	 * 
	 * @param time
	 */
	public static void pause(int time) {
		try {
			Thread.sleep(time * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Convert Date format as per CTRIP source.
	 * 
	 * @param givenDate
	 * @return
	 */
	public static Date convertCtripStringToDate(String givenDate) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date givenMentionDate = null;
		try {
			givenMentionDate = simpleDateFormat.parse(givenDate.replaceAll("[^0-9-]", ""));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		return givenMentionDate;
	}

}