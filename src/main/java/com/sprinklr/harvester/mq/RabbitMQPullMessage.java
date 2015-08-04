package com.sprinklr.harvester.mq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;//JSONArray;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.sprinklr.harvester.model.ReviewData;
import com.sprinklr.harvester.util.PropertyHandler;
import com.sprinklr.harvester.util.StaticUtils;
//import org.json.JSONObject;
//import org.json.parser.JSONParser;

/**
 * Rabbit MQ reusable function for pulling into queue.
 *
 */
public class RabbitMQPullMessage {

	public final static String QUEUE_NAME = PropertyHandler.getProperties().getProperty("pull_queue");

	/**
	 * Method to read data in JSON Format & extract values out of it and returns
	 * a HashMap
	 * 
	 * @param message
	 *            JSON File data in String format
	 * @return HashMap<String,ArrayList<ReviewData>>
	 */
	public static HashMap<String, ArrayList<ReviewData>> writeJsonToHashmap(String message) {
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

				System.out.println(rdObject.toString());

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
	 * @return
	 */
	public static String getHarvesterID(String message) {
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

	/**
	 * Pull the rabbitMQ messages and parse that JSON message.
	 * 
	 * @return
	 */
	public static HashMap<String, HashMap<String, ArrayList<ReviewData>>> pull() {

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

			while (true) {
				count++;
				if (count < 4) {
					QueueingConsumer.Delivery delivery = consumer.nextDelivery(50000);
					if (delivery == null) {
						continue;
					}
					String message = new String(delivery.getBody());
					System.out.println(" [x] Received ->> " + message);
					String stubID = getHarvesterID(message);
					HashMap<String, ArrayList<ReviewData>> actualDataByAuthorID = writeJsonToHashmap(message);
					actualReviewDataPerStub.put(stubID, actualDataByAuthorID);
				} else {
					break;
				}
			}
			System.out.println("++++++++++++++++++++++++out of the loop++++++++++++++++++++++++++++++");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (channel != null) {
				try {
					channel.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return actualReviewDataPerStub;
	}
}