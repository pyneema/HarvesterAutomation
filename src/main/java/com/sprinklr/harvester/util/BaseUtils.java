package com.sprinklr.harvester.util;

public class BaseUtils {

	public BaseUtils() {
		System.out.println("=== In constructor Base utiotls => " + getCurrentPath().toString());
		System.setProperty("log.path", getCurrentPath());
	}

	static String getCurrentPath() {
		return System.getProperty("user.dir") + "/logs";
	}
}
