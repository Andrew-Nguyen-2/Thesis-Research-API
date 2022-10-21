package rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rabbitmq.client.ConnectionFactory;

import constants.Constants;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import message.Message;
import user.User;

public class RabbitMQConnection {
	
	private User user;
	private Connection connection;
	private Channel channel;
	private String queueName;
	
	
	public RabbitMQConnection(User user) throws IOException, TimeoutException {
		this.user = user;
		ConnectionFactory factory = new ConnectionFactory();     
		factory.setHost("localhost");                            
		connection = factory.newConnection();        
		channel = connection.createChannel();                    
		                                                         
		channel.exchangeDeclare(Constants.EXCHANGE_NAME, Constants.EXCHANGE_TYPE);              
		queueName = channel.queueDeclare().getQueue();    
		                                                         
		channel.queueBind(queueName, Constants.EXCHANGE_NAME, Constants.ANNOUNCE_ROUTING_KEY);      
		channel.queueBind(queueName, Constants.EXCHANGE_NAME, this.user.getUserID());
	}
	
	public void announce(Message message) {
		try {
			channel.basicPublish(Constants.EXCHANGE_NAME, Constants.ANNOUNCE_ROUTING_KEY, null, message.toJSON().getBytes());
			String sent = String.format(" [x] Sent %s%n", message.toJSON());
			sent += "To: Everyone\n";
			Constants.LOGGER.log(Level.ALL, sent);
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
			channel.basicPublish(Constants.EXCHANGE_NAME, userID, null, message.toJSON().getBytes());
			String sent = String.format(" [x] Sent %s%n", message.toJSON());
			sent += String.format("To: %s%n", userID);
			Constants.LOGGER.log(Level.ALL, sent);
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
	
	public Channel getChannel() {
		return this.channel;
	}
	
	public String getQueueName() {
		return this.queueName;
	}

}
