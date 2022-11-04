package api;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
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
import message.Wormhole.ReceiveObj;
import rabbitmq.RabbitMQConnection;
import user.User;


public class ResearchAPI {
	
	private User 				user;
	private RabbitMQConnection 	connection;
	private Queue<String> 		messageQueue;
	private String				username;
	private String				receivedFilename;
	
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
	 * Add the file path of data to share and make an announcement.
	 * 
	 * @param filepath		The full file path of the data.
	 */
	public void addFile(String filepath) {
		File file = new File(filepath);
		if (!file.exists()) {
			System.out.println("ERROR: filepath does not exist for: '" + filepath + "'");
			return;
		}
		this.user.addFilepaths(filepath);
		if (this.connection == null) {
			connect(this.username);
		}
		Message announceData = new Message(this.user.getUserID(), Constants.ANNOUNCE_MESSAGE);
		for (Path path : this.user.getFilepaths()) {
			announceData.addFilePath(path.toString());
		}
		announceData.addContent("I have data.");
		this.connection.announce(announceData);
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
	 * 
	 * @param username		The username of account on jlabdaq.
	 */
	public void connect(String username) {
		try {
			this.username = username;
			this.connection = new RabbitMQConnection(this.user, this.username);
			MessageThread messageThread = new MessageThread(this);
			messageThread.start();
			Constants.LOGGER.log(Level.ALL, " [*] Began listening to RabbitMQ server.%n");
			System.out.println(" [*] Began listening to RabbitMQ server. \n");
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
	 * Wait until a message is received and get the message if one exists.
	 * After processing the message, if user received a file, wait
	 * until the file has finished being transfered then set the received file name.
	 */
	public void getNextMessage() {
		if (!messageQueue.isEmpty()) {
			String message = messageQueue.remove();
			ProcessMessage processMessage = new ProcessMessage(user, this.connection, message);
			
			// receivedObj contains the filename received and the running thread or null if user did not receive a file
			ReceiveObj receiveObj = processMessage.process();
			if (receiveObj != null) {
				// wait until thread is finished before setting the filename
				while(receiveObj.getRunningThread().isAlive()) {
					
				}
				this.receivedFilename = receiveObj.getFilename();
			}
		}
	}
	
	/**
	 * Get the received file path.
	 * 
	 * @return		The path where the file received is located.
	 */
	public String getReceivedFilepath() {
		String cwd = System.getProperty("user.dir");
		File dir = new File(cwd, "received-files");
		if (this.receivedFilename != null && dir.exists() && Arrays.asList(dir.list()).contains(this.receivedFilename)) {
			File file = new File(dir, this.receivedFilename);
			this.receivedFilename = null;
			return file.toString();
		}
		return null;
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
					}
				};
				
				channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}

