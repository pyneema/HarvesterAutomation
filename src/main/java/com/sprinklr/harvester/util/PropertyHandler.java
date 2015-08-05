package com.sprinklr.harvester.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Class to read properties files values
 */
public class PropertyHandler {

	public static final Logger LOGGER = Logger.getLogger(PropertyHandler.class);
	public static Properties harvProperties = null;
	public static Properties sourceProperties = null;

	/**
	 * Method to fetch configuration properties from qa-db.properties file
	 * 
	 * @return java.util.Properties
	 */
	public static Properties getProperties() {
		if (harvProperties != null) {
			return harvProperties;
		} else {
			harvProperties = new Properties();
		}
		LOGGER.info("**getProperties** - getting the properties from Harvester.properties");
		String prop_file_name = System.getProperty("user.dir") + "\\conf\\Harvester.properties";
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(prop_file_name);
			harvProperties.load(inputStream);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return harvProperties;
	}

	/**
	 * Method to get property values from <source>.properties under
	 * src/main/resources
	 * 
	 * @return java.util.Properties
	 */
	public static Properties getSourceProperties() {
		if (sourceProperties != null) {
			return sourceProperties;
		} else {
			sourceProperties = new Properties();
		}

		String source = PropertyHandler.getProperties().getProperty("source").toLowerCase();
		LOGGER.info("**getSourceProperties** - getting the source properties ==> " + source);
		String prop_file_name = System.getProperty("user.dir") + "\\src\\main\\resources\\" + source + "\\" + source
		        + ".properties";
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(prop_file_name);
			sourceProperties.load(inputStream);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sourceProperties;
	}

}
