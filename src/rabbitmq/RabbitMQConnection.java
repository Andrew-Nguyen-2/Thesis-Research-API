package rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import constants.Constants;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import message.Message;
import user.User;

/**
 * Connect to the RabbitMQ server.
 * @author andrewnguyen
 *
 */
public class RabbitMQConnection {
	
	private static final String EXCHANGE_NAME 			= "milestone1";
	private static final String EXCHANGE_TYPE 			= "topic";
	private static final String ANNOUNCE_ROUTING_KEY	= "announce";
	
	private static final String	SENT 					= " [x] Sent ";
	
	private User 				user;
	private Connection			connection;
	private Channel 			channel;
	private String 				queueName;
	
	
	/**
	 * Constructor for creating a RabbitMQConnection.
	 * 
	 * @param user				The user connecting to the RabbitMQ server.
	 * @param username			The username of account on jlabdaq.
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public RabbitMQConnection(User user, String username) throws IOException, TimeoutException {
		this.user = user;
		ConnectionFactory factory = new ConnectionFactory();
		// TODO implement ssh tunneling
//		String ssh = "ssh -L 5672:localhost:5672 " + username + "@jlabdaq.pcs.cnu.edu";
//		Executive.execute(ssh, null);
		factory.setHost("localhost");
//		factory.setHost("137.155.2.201");
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
	 * @param message		The message to be sent.
	 */
	public void announce(Message message) {
		try {
			channel.basicPublish(EXCHANGE_NAME, ANNOUNCE_ROUTING_KEY, null, message.toJSON().getBytes());
			String sent = SENT + message;
			Constants.LOGGER.log(Level.ALL, sent);
			System.out.print(sent);
			System.out.println("To: Everyone\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Send a direct message to another user.
	 * 
	 * @param message		The message to be sent.
	 * @param userID		The ID of the intended user.
	 */
	public void direct(Message message, String userID) {
		try {
			channel.basicPublish(EXCHANGE_NAME, userID, null, message.toJSON().getBytes());
			String sent = SENT + message;
			Constants.LOGGER.log(Level.ALL, sent);
			System.out.print(sent);
			System.out.println("To: " + userID + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the channel of the instance.
	 * 
	 * @return		A Channel instance.
	 */
	public Channel getChannel() {
		return this.channel;
	}
	
	/**
	 * Get the name of the queue the user is connected to.
	 * 
	 * @return		The name of the queue.
	 */
	public String getQueueName() {
		return this.queueName;
	}

}
