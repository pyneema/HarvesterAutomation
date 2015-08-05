package com.sprinklr.harvester.mq;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.testng.Assert;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sprinklr.harvester.model.InitialData;
import com.sprinklr.harvester.util.PropertyHandler;

/**
 * Rabbit MQ reusable function for pushing into queue.
 *
 */
public class RabbitMQPushMessage {

	public static Logger LOGGER = Logger.getLogger(RabbitMQPushMessage.class);
	public static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String QUEUE_NAME = PropertyHandler.getProperties().getProperty("push_queue");
	public static final String CLIENT = PropertyHandler.getProperties().getProperty("client");
	public static final String DATE = PropertyHandler.getProperties().getProperty("date") + "T05:00:00Z";
	public static final String MQHOST = PropertyHandler.getProperties().getProperty("mqhost");
	public static final String TTL = "86400000";

	/**
	 * Pushing the messages into rabbitmq.
	 * 
	 * @param testData
	 */
	public static void push(HashMap<Integer, InitialData> testData) {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(MQHOST);
		Connection connection = null;
		Channel channel = null;

		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.queueDeclare(QUEUE_NAME, true, false, false, null);

			Set<Integer> testDataKeys = testData.keySet();
			Iterator<Integer> testDataKey = testDataKeys.iterator();
			Integer stubId;
			while (testDataKey.hasNext()) {
				stubId = testDataKey.next();
				if (testData.get(stubId).isValidStub()) {
					String message = getJsonCmd(stubId);
					LOGGER.info("push() pushing Json message to MQ : " + message);
					channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
					LOGGER.info("push() Done, 1 task dispatched. StubID ==> " + stubId);
				} else {
					LOGGER.info("push() Done, 0 task dispatched. As condition does not match. For URL ==> "
					        + testData.get(stubId).getStubURL());
					Assert.assertTrue(false, "Failing the case as condition does not match, for URL ==> "
					        + testData.get(stubId).getStubURL());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	}

	/**
	 * Generating the JSON message from the given stub id.
	 * 
	 * @param stubId
	 * @return
	 */
	public static String getJsonCmd(Integer stubId) {
		String timeStamp = getUTCdatetimeAsString().replace(" ", "T") + "Z";

		String jsonToPush = "{\"timestamp\":\"" + timeStamp + "\",\"parameters\":{\"client\":\"" + CLIENT
		        + "\",\"stubId\":" + stubId + ",\"from\":\"" + DATE + "\",\"limit\":1000},\"ttl\":" + TTL + "}";

		LOGGER.info("JsonPush.getJsonCmd() : returning command '" + jsonToPush);
		return jsonToPush;
	}

	/**
	 * Generate the date sting in UTC format.
	 * 
	 * @return
	 */
	public static String getUTCdatetimeAsString() {
		final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		final String utcTime = sdf.format(new Date());

		return utcTime;
	}
}
