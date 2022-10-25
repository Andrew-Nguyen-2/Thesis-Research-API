package api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import constants.Constants;
import message.Message;
import message.ProcessMessage;
import rabbitmq.RabbitMQConnection;
import user.User;


public class ResearchAPI {
	
	private User 								user;
	private RabbitMQConnection 					connection;
	private Queue<String> 						messageQueue;
	
	/**
	 * Constructor for creating a ResearchAPI instance.
	 */
	public ResearchAPI() {
		this.user = new User();
		this.messageQueue = new LinkedList<>();
	}
	
	/**
	 * Add the formats wanted.
	 * 
	 * @param wantFormats		Formats wanted.
	 */
	public void addWantFormats(String ... wantFormats) {
		this.user.addWant(wantFormats);
	}
	
	/**
	 * Add the file path of data to share.
	 * 
	 * @param filepath		The full file path of the data.
	 */
	public void addFile(String filepath) {
		this.user.addFilepaths(filepath);
	}
	
	/**
	 * Add the formats that can be translated.
	 * 
	 * @param originalFormat		The original data format.
	 * @param destinationFormat		The translated data format.
	 */
	public void addConvertFormat(String originalFormat, String destinationFormat) {
		this.user.addConvert(originalFormat, destinationFormat);
	}
	
	/**
	 * Connect to the RabbitMQ server.
	 */
	public void connect() {
		try {
			this.connection = new RabbitMQConnection(this.user);
			if (!this.user.getFilepaths().isEmpty()) {
				Message announceData = new Message(this.user.getUserID(), Constants.ANNOUNCE_MESSAGE);
				for (Path filepath : this.user.getFilepaths()) {
					announceData.addFilePath(filepath.toString());
				}
				announceData.addContent("I have data.");
				this.connection.announce(announceData, true);
			}
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the RabbitMQConnection instance.
	 * 
	 * @return		The RabbitMQConnection instance.
	 */
	public RabbitMQConnection getConnection() {
		return this.connection;
	}
	
	/**
	 * Implements a MessageThread and sleeps until notified of a new message.
	 * 
	 * @return		The message received.
	 */
	public void getNextMessage() {
		MessageThread messageThread = new MessageThread(this);
		messageThread.start();
		Constants.LOGGER.log(Level.ALL, " [*] Began listening to RabbitMQ server.%n");
		System.out.println(" [*] Began listening to RabbitMQ server. \n");
		while (true) {
			synchronized (this) {
				while (messageQueue.isEmpty()) {
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						Thread.currentThread().interrupt();
					}
				}
				try {
					String message = messageQueue.remove();
					ProcessMessage processMessage = new ProcessMessage(user, message);
					processMessage.process();
				} catch (NoSuchElementException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class MessageThread extends Thread {
		
		private ResearchAPI 		researchAPI;
		private RabbitMQConnection 	connection;
		private Channel 			channel;
		private String 				queueName;
		
		/**
		 * Constructor for creating a MessageThread.
		 * 
		 * @param resAPI		The ResearchAPI instance.
		 */
		private MessageThread(ResearchAPI resAPI) {
			this.researchAPI = resAPI;
			this.connection = this.researchAPI.getConnection();
			this.channel = this.connection.getChannel();
			this.queueName = this.connection.getQueueName();
		}
		
		@Override
		public void run() {
			try {
				DeliverCallback deliverCallback = (consumerTag, delivery) -> {
					String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
					synchronized (this.researchAPI) {
						this.researchAPI.messageQueue.add(message);
						this.researchAPI.notifyAll();
					}
				};
				
				channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}

