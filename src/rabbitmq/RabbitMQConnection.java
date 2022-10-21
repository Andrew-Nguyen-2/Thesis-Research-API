package rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import message.Message;
import user.User;

public class RabbitMQConnection {
	
	private static final String EXCHANGE_NAME 			= "milestone1";
	private static final String EXCHANGE_TYPE 			= "topic";
	
	private static final String ANNOUNCE_ROUTING_KEY 	= "announce";
	
	private User user;
	private Connection connection;
	private Channel channel;
	
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	
	public RabbitMQConnection(User user) throws IOException, TimeoutException {
		this.user = user;
		ConnectionFactory factory = new ConnectionFactory();     
		factory.setHost("localhost");                            
		connection = factory.newConnection();        
		channel = connection.createChannel();                    
		                                                         
		channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);              
		String queueName = channel.queueDeclare().getQueue();    
		                                                         
		channel.queueBind(queueName, EXCHANGE_NAME, ANNOUNCE_ROUTING_KEY);      
		channel.queueBind(queueName, EXCHANGE_NAME, this.user.getUserID());
	}
	
	public void announce(Message message) {
		try {
			channel.basicPublish(EXCHANGE_NAME, ANNOUNCE_ROUTING_KEY, null, message.toJSON().getBytes());
			String sent = String.format(" [x] Sent %s%n", message.toJSON());
			sent += "To: Everyone\n";
			LOGGER.log(Level.ALL, sent);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void direct(Message message, String userID) {
		try {
			channel.basicPublish(EXCHANGE_NAME, userID, null, message.toJSON().getBytes());
			String sent = String.format(" [x] Sent %s%n", message.toJSON());
			sent += String.format("To: %s%n", userID);
			LOGGER.log(Level.ALL, sent);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
