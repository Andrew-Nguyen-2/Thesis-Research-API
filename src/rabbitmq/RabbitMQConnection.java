package rabbitmq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import constants.Constants;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import message.Executive;
import message.Message;
import user.User;

/**
 * Connect to the RabbitMQ server.
 * @author andrewnguyen
 *
 */
public class RabbitMQConnection {
	
	private static final String EXCHANGE_NAME 			= "milestone1";
	private static final String EXCHANGE_TYPE 			= "direct";
	private static final String ANNOUNCE_ROUTING_KEY	= "announce";
	private static final String	SENT 					= " [x] Sent ";
	
	private static final String HOST_SERVER				= "jlabdaq.pcs.cnu.edu";
	private static final String LOCAL_HOST				= "localhost";
	private static final int 	SSH_PORT				= 22;
	private static final int	MESSAGE_PORT			= 5672;
	
	private static final File 	HOME 					= new File(System.getProperty("user.home"));
	private static final File 	SSH_DIRECTORY 			= new File(HOME, ".ssh");
	private static final File 	KNOWN_HOSTS_DIRECTORY	= new File(SSH_DIRECTORY, "known_hosts");
	
	private User 				user;
	private Connection			connection;
	private Channel 			channel;
	private String 				queueName;
	
	private Session				session;
	
	
	/**
	 * Constructor for creating a RabbitMQConnection.
	 * 
	 * @param user				The user connecting to the RabbitMQ server.
	 * @param username			The username of account on jlabdaq.
	 * @param password			The password for the account on jlabdaq.
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public RabbitMQConnection(User user, String username, String password) throws IOException, TimeoutException {
		this.user = user;
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(LOCAL_HOST);
		
		connection = factory.newConnection();
		channel = connection.createChannel();
                 
		channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
		queueName = channel.queueDeclare().getQueue();
		                                                         
		channel.queueBind(queueName, EXCHANGE_NAME, ANNOUNCE_ROUTING_KEY);
		channel.queueBind(queueName, EXCHANGE_NAME, this.user.getUserID());
	}
	
	/**
	 * Check known_hosts file on user's machine
	 */
	private void checkKnownHosts() {
		// create ssh directory if it does not exist
		if (!SSH_DIRECTORY.exists()) {
			Executive.execute("mkdir .ssh", HOME);
		}
		
		Thread generateRSA;
		// Return if rsa key exists for jlabdaq server
		// generate rsa key if one does not exist in known_hosts file or if known_hosts does not exist
		try (Scanner myReader = new Scanner(KNOWN_HOSTS_DIRECTORY)) {
			while (myReader.hasNextLine()) {
				String[] data = myReader.nextLine().split(" ");
				if (data[0].equals(HOST_SERVER) && data[1].equals("ssh-rsa")) {
					return;
				}
			}
			generateRSA = Executive.execute("ssh-keyscan -t rsa jlabdaq.pcs.cnu.edu >> known_hosts", SSH_DIRECTORY);
		} catch (FileNotFoundException e) {
			generateRSA = Executive.execute("ssh-keyscan -t rsa jlabdaq.pcs.cnu.edu >> known_hosts", SSH_DIRECTORY);
		}
		
		while (generateRSA.isAlive()) {
			// wait until rsa key has been generated
		}
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
