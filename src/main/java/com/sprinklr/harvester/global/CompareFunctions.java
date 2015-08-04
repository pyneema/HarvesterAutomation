package com.sprinklr.harvester.global;

import java.util.ArrayList;
import java.util.HashMap;

import org.testng.Assert;

import com.sprinklr.harvester.model.ReviewData;

public class CompareFunctions {

	public static void compareData(HashMap<String, HashMap<String, ArrayList<ReviewData>>> aData,
	        HashMap<String, HashMap<String, ArrayList<ReviewData>>> eData) {
		System.out.println("Actual Data Key sets = " + aData.size() + " - " + aData.keySet().toString());
		System.out.println("Expected Data Key sets = " + eData.size() + " - " + eData.keySet().toString());

		Assert.assertEquals(aData.keySet().size(), eData.keySet().size(), "StubID size does not match: Actual Stub IDs = "
		        + aData.keySet().toString() + " <====> Expected Stub IDs = " + eData.keySet().toString());
		for (String expectedStubid : eData.keySet()) {
			Assert.assertTrue(aData.containsKey(expectedStubid), "Stub ID does not found: " + expectedStubid + " in "
			        + aData.keySet().toString());

			HashMap<String, ArrayList<ReviewData>> actualData = aData.get(expectedStubid);
			HashMap<String, ArrayList<ReviewData>> expectedData = eData.get(expectedStubid);

			Assert.assertEquals(actualData.keySet().size(), expectedData.keySet().size(),
			        "Authors size does not match: Actual Author IDs = " + actualData.keySet().toString()
			                + " <====>- Expected Author IDs = " + expectedData.keySet().toString());
			for (String expectedAuthor : expectedData.keySet()) {
				System.out.println("Expected Author ID = " + expectedAuthor);
				Assert.assertTrue(actualData.containsKey(expectedAuthor),
				        "Author ID does not found in actual data list: " + expectedAuthor + " in "
				                + actualData.keySet().toString());

				ArrayList<ReviewData> actualReviewList = actualData.get(expectedAuthor);
				ArrayList<ReviewData> expectedReviewList = expectedData.get(expectedAuthor);

				Assert.assertEquals(actualReviewList.size(), expectedReviewList.size(),
				        "Review size does not match: Actual reviews per Author = " + actualReviewList.toString()
				                + " <====> Expected Reviews per Author = " + expectedReviewList.toString());
				for (ReviewData expectedReviewData : expectedReviewList) {
					boolean flag = false;
					ReviewData reviewData = null;
					for (ReviewData actualReviewData : actualReviewList) {
						reviewData = actualReviewData;
						System.out.println("Expected Review Data AuthorID = " + expectedReviewData.getAuthorId());
						System.out.println("Actual Review Data Author ID = " + actualReviewData.getAuthorId());

						System.out.println("Expected Review Data Mention Date = "
						        + expectedReviewData.getMentionedDate());
						System.out.println("Actual Review Data Mention Date = " + actualReviewData.getMentionedDate());

						System.out.println("Expected Review Data Rating = " + expectedReviewData.getRatings());
						System.out.println("Actual Review Data Rating = " + actualReviewData.getRatings());

						System.out.println("Expected Review Data Comments = " + expectedReviewData.getComment());
						System.out.println("Actual Review Data Comments = " + actualReviewData.getComment());

						if (expectedReviewData.getAuthorId().trim()
						        .equalsIgnoreCase(actualReviewData.getAuthorId().trim())
						        && (expectedReviewData.getComment().trim()
						                .contains(actualReviewData.getComment().trim()) || actualReviewData
						                .getComment().trim().contains(expectedReviewData.getComment().trim()))) {
							System.out
							        .println("===============================================================================");
							flag = true;
							break;
						}
					}
					Assert.assertTrue(flag,
					        "Review not found: " + reviewData.getAuthorId() + " --> " + reviewData.getComment());
				}
			}
		}
	}
}
