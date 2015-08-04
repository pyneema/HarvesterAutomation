package com.sprinklr.harvester.mq;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;

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

	public final static String QUEUE_NAME = PropertyHandler.getProperties().getProperty("push_queue");
	public final static String CLIENT = PropertyHandler.getProperties().getProperty("client");
	public final static String DATE = PropertyHandler.getProperties().getProperty("date") + "T05:00:00Z";
	public final static String MQHOST = PropertyHandler.getProperties().getProperty("mqhost");
	public final static String TTL = "86400000";

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
				
				String message = getJsonCmd(stubId);
				LOGGER.info("push() pushing Json message to MQ : " + message);
				channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
				LOGGER.info("push() Done, 1 tasks dispatched");
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	}

	public static String getJsonCmd(Integer stubId) {
		String timeStamp = GetUTCdatetimeAsString().replace(" ", "T") + "Z";

		String jsonToPush = "{\"timestamp\":\"" + timeStamp + "\",\"parameters\":{\"client\":\"" + CLIENT
		        + "\",\"stubId\":" + stubId + ",\"from\":\"" + DATE + "\",\"limit\":1000},\"ttl\":" + TTL + "}";

		LOGGER.info("JsonPush.getJsonCmd() : returning command '" + jsonToPush);
		return jsonToPush;
	}

	public static String GetUTCdatetimeAsString() {
		final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		final String utcTime = sdf.format(new Date());

		return utcTime;
	}

}
