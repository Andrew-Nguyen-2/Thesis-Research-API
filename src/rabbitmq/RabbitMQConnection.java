package rabbitmq;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import api.ResearchAPI;
import constants.Constants;
import logging.Log;
import message.Message;
import user.User;

/**
 * Connect to the RabbitMQ server.
 * 
 * @author andrewnguyen
 *
 */
public class RabbitMQConnection {

	private static final String CLASS_NAME = RabbitMQConnection.class.getName();
	private static final String RESEARCH_API_CONNECT = ResearchAPI.class.getName() + ":connect";

	private static final String EXCHANGE_NAME = "research";
	private static final String EXCHANGE_TYPE = "direct";
	private static final String ANNOUNCE_ROUTING_KEY = "announce";
	private static final String SENT = " [x] Sent ";

	private User user;
	private Connection connection;
	private Channel channel;
	private String queueName;

	/**
	 * Constructor for creating a RabbitMQConnection.
	 * 
	 * @param user The user connecting to the RabbitMQ server.
	 * @param uri  The uri for the RabbitMQ server.
	 * @throws IOException
	 * @throws TimeoutException
	 * @throws URISyntaxException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public RabbitMQConnection(User user, String uri) {
		this.user = user;

		ConnectionFactory factory = null;

		if (!uri.isEmpty()) {
			try {
				factory = getConnectionFactory(uri);
			} catch (KeyManagementException | NoSuchAlgorithmException | NullPointerException | URISyntaxException e) {
				Log.error("Failed to connect to RabbitMQ server: " + uri, RESEARCH_API_CONNECT);
				Log.other("Connecting to default RabbitMQ server");

				// attempt to connect to default server
				factory = getDefaultConnectionFactory();
				if (factory == null) {
					return;
				}
			}
		}

		try {
			connection = factory.newConnection();
			if (connection == null) {
				factory = getDefaultConnectionFactory();
				connection = factory.newConnection();
			}
			channel = connection.createChannel();
			channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
			queueName = channel.queueDeclare().getQueue();

			channel.queueBind(queueName, EXCHANGE_NAME, ANNOUNCE_ROUTING_KEY);
			channel.queueBind(queueName, EXCHANGE_NAME, this.user.getUserID());
		} catch (IOException | TimeoutException e) {
			Log.error("Failed establishing connection and queues to RabbitMQ server, please double check input URI",
					RESEARCH_API_CONNECT);
		}

	}

	/**
	 * Create ConnectionFactory object from user inputted uri.
	 * 
	 * @param uri The uri for the RabbitMQ server.
	 * @return The ConnectionFactory object.
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws URISyntaxException
	 */
	private ConnectionFactory getConnectionFactory(String uri)
			throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUri(uri);
		return factory;
	}

	/**
	 * Create the default ConnectionFactory object.
	 * 
	 * @return The default ConnectionFactory.
	 */
	private ConnectionFactory getDefaultConnectionFactory() {
		ConnectionFactory factory = new ConnectionFactory();
		try {
			factory.setUri(Constants.RABBITMQ_URI);
		} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e) {
			Log.error("Failed to connect to default RabbitMQ server", RESEARCH_API_CONNECT);
			return null;
		}
		return factory;
	}

	/**
	 * Announce a message to all users connected.
	 * 
	 * @param message The message to be sent.
	 */
	public void announce(Message message) {
		try {
			channel.basicPublish(EXCHANGE_NAME, ANNOUNCE_ROUTING_KEY, null, message.toJSON().getBytes());
			String sent = SENT + message;
			Log.sent(sent);
		} catch (IOException e) {
			Log.error(e.getMessage(), CLASS_NAME + ":" + ANNOUNCE_ROUTING_KEY);
		}
	}

	/**
	 * Send a direct message to another user.
	 * 
	 * @param message The message to be sent.
	 * @param userID  The ID of the intended user.
	 */
	public void direct(Message message, String userID) {
		try {
			channel.basicPublish(EXCHANGE_NAME, userID, null, message.toJSON().getBytes());
			String sent = SENT + message;
			Log.sent(sent);
		} catch (IOException e) {
			Log.error(e.getMessage(), CLASS_NAME + ":" + EXCHANGE_TYPE);
		}
	}

	/**
	 * Get the channel of the instance.
	 * 
	 * @return A Channel instance.
	 */
	public Channel getChannel() {
		return this.channel;
	}

	/**
	 * Get the name of the queue the user is connected to.
	 * 
	 * @return The name of the queue.
	 */
	public String getQueueName() {
		return this.queueName;
	}
}
