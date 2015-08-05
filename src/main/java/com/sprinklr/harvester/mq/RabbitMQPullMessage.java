package com.sprinklr.harvester.mq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;//JSONArray;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.sprinklr.harvester.model.ReviewData;
import com.sprinklr.harvester.util.PropertyHandler;

/**
 * Rabbit MQ reusable function for pulling into queue.
 *
 */
public class RabbitMQPullMessage {

	public static final Logger LOGGER = Logger.getLogger(RabbitMQPullMessage.class);

	public static final String QUEUE_NAME = PropertyHandler.getProperties().getProperty("pull_queue");

	public static final int MAX_RETRY = 5;

	/**
	 * Pull the rabbitMQ messages and parse that JSON message.
	 * 
	 * @return - Hashmaped data from the Rabbit MQ JOSON message.
	 */
	public static HashMap<String, HashMap<String, ArrayList<ReviewData>>> pull() {
		LOGGER.info("**pull** - Pull all the messages from the queue.");
		HashMap<String, HashMap<String, ArrayList<ReviewData>>> actualReviewDataPerStub = new HashMap<String, HashMap<String, ArrayList<ReviewData>>>();
		Integer count = 0;
		Connection connection = null;
		Channel channel = null;

		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(PropertyHandler.getProperties().getProperty("mqhost"));
			connection = factory.newConnection();
			channel = connection.createChannel();

			channel.queueDeclare(QUEUE_NAME, true, false, false, null);
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(QUEUE_NAME, true, consumer);

			String message = "";
			while (true) {
				count++;
				if (count < MAX_RETRY) {
					QueueingConsumer.Delivery delivery = consumer.nextDelivery(80000);
					if (delivery == null) {
						continue;
					}
					message = new String(delivery.getBody());
					LOGGER.info(" [x] Received ->> " + message);
					if (message.contains("UnsupportedPagePatternException")) {
						String stubID = getHarvesterID(message);
						HashMap<String, ArrayList<ReviewData>> actualDataByAuthorID = writeJsonToHashmap(message);
						actualReviewDataPerStub.put(stubID, actualDataByAuthorID);
						continue;
					}
					String stubID = getHarvesterID(message);
					HashMap<String, ArrayList<ReviewData>> actualDataByAuthorID = writeJsonToHashmap(message);
					actualReviewDataPerStub.put(stubID, actualDataByAuthorID);
					break;
				} else {
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (channel != null) {
				try {
					channel.close();
				} catch (Exception e) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
				}
			}
		}
		return actualReviewDataPerStub;
	}

	/**
	 * Method to read data in JSON Format & extract values out of it and returns
	 * a HashMap
	 * 
	 * @param message
	 *            JSON File data in String format
	 * @return HashMap<String,ArrayList<ReviewData>>
	 */
	public static HashMap<String, ArrayList<ReviewData>> writeJsonToHashmap(String message) {
		LOGGER.info("**writeJsonToHashmap** - Writing JSON data into hashmap.");
		HashMap<String, ArrayList<ReviewData>> reviewContent = new HashMap<String, ArrayList<ReviewData>>();

		try {
			JSONObject obj = new org.json.JSONObject(message);
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray jsonEntries = (JSONArray) jsonObject.get("entries");
			JSONObject record = (JSONObject) jsonObject.get("record");

			for (int i = 0; i < jsonEntries.length(); i++) {
				ReviewData rdObject = new ReviewData();
				rdObject.setHarvesterID(record.get("harvesterId").toString());
				JSONObject entry = (JSONObject) jsonEntries.get(i);
				JSONObject author = (JSONObject) entry.get("author");
				rdObject.setAuthorId(author.get("authorId").toString().trim());
				JSONObject document = (JSONObject) entry.get("document");
				rdObject.setMentionedDate(document.get("mentionTime").toString().trim());
				rdObject.setRatings(document.get("overallRating").toString().trim());
				rdObject.setComment(document.get("content").toString().trim().replaceAll("  +", " "));
				rdObject.setHarvesterID(record.get("harvesterId").toString());

				// System.out.println(rdObject.toString());

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

		} catch (Exception e) {
			e.printStackTrace();
		}

		return reviewContent;
	}

	/**
	 * Get harvester ID for the given stub/URL.
	 * 
	 * @param message
	 * @return - Harvester ID.
	 */
	public static String getHarvesterID(String message) {
		LOGGER.info("**getHarvesterID** - Get the harvester id from the given JSON message.");
		try {
			JSONObject obj = new JSONObject(message);
			JSONObject jsonObject = (JSONObject) obj;
			JSONObject record = (JSONObject) jsonObject.get("record");
			String harvesterid = record.get("harvesterId").toString();
			return harvesterid;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "0";
	}
}