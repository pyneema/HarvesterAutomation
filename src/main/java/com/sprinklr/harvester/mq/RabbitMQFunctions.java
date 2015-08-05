package com.sprinklr.harvester.mq;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sprinklr.harvester.util.PropertyHandler;

/**
 * 
 * Reusable functions for rabbit mq.
 *
 */
public class RabbitMQFunctions {

	public static final Logger LOGGER = Logger.getLogger(RabbitMQFunctions.class);

	/**
	 * Purge the given queue.
	 * 
	 * @param queue
	 *            - Queue name.
	 */
	public static void purgeQueue(final String queue) {
		LOGGER.info("Purging the messages from queue ==> " + queue);
		Connection connection = null;
		Channel channel = null;

		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(PropertyHandler.getProperties().getProperty("mqhost"));
			connection = factory.newConnection();
			channel = connection.createChannel();

			channel.queueDeclare(queue, true, false, false, null);
			channel.queuePurge(queue);
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
	}

}
