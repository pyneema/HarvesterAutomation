package com.sprinklr.harvester.model;

/**
 * POJO for saving the test data taken from the CSV file and the DB.
 */
public class InitialData {

	private int stubID;
	private String stubURL;
	private String stubEndpoint;
	private boolean isValidStub = false;

	public int getStubID() {
		return stubID;
	}

	public void setStubID(int stubID) {
		this.stubID = stubID;
	}

	public String getStubURL() {
		return stubURL;
	}

	public void setStubURL(String stubURL) {
		this.stubURL = stubURL;
	}

	public String getStubEndpoint() {
		return stubEndpoint;
	}

	public void setStubEndpoint(String stubEndpoint) {
		this.stubEndpoint = stubEndpoint;
	}

	public boolean isValidStub() {
	    return isValidStub;
    }

	public void setValidStub(boolean isValidStub) {
	    this.isValidStub = isValidStub;
    }

}
