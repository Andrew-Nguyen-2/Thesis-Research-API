package rabbitmq;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

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
	 * @throws IOException
	 * @throws TimeoutException
	 * @throws URISyntaxException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public RabbitMQConnection(User user)
			throws IOException, TimeoutException, KeyManagementException, NoSuchAlgorithmException, URISyntaxException {
		this.user = user;
		ConnectionFactory factory = new ConnectionFactory();

		factory.setUri(Constants.RABBITMQ_URI);

		connection = factory.newConnection();
		channel = connection.createChannel();

		channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
		queueName = channel.queueDeclare().getQueue();

		channel.queueBind(queueName, EXCHANGE_NAME, ANNOUNCE_ROUTING_KEY);
		channel.queueBind(queueName, EXCHANGE_NAME, this.user.getUserID());
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
