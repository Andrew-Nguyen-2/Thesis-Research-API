package api;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
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


/**
 * The entry point into the api.
 */
public class ResearchAPI {
	
	private User 				user;
	private RabbitMQConnection 	connection;
	private String				username;
	private String				receivedFilename;
	
	/**
	 * Constructor for creating a ResearchAPI instance.
	 */
	public ResearchAPI() {
		this.user = new User();
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
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Start listening for messages.
	 */
	public void startListening() {
		(new MessageThread()).start();
		Constants.LOGGER.log(Level.ALL, " [*] Began listening to RabbitMQ server.%n");
		System.out.println(" [*] Began listening to RabbitMQ server. \n");
	}
	
	/**
	 * Get the received file path.
	 * Place method call in a while true loop to continuously check if file has been received.
	 * 
	 * @return		The path where the file received is located.
	 */
	public String getReceivedFilepath() {
		String cwd = System.getProperty("user.dir");
		File dir = new File(cwd, "received-files");
		// check receivedFilename is set, received files directory exists, and filename exists within the directory
		if (this.receivedFilename != null && dir.exists() && Arrays.asList(dir.list()).contains(this.receivedFilename)) {
			File file = new File(dir, this.receivedFilename);
			return file.toString();
		}
		return null;
	}
	
	/**
	 * Get the received file path.
	 * Place method call in a while true loop to continuously check if file has been received.
	 * 
	 * @return		The path where the file received is located.
	 */
	public String getReceivedFileFormat() {
		String cwd = System.getProperty("user.dir");
		File dir = new File(cwd, "received-files");
		// check receivedFilename is set, received files directory exists, and filename exists within the directory
		if (this.receivedFilename != null && dir.exists() && Arrays.asList(dir.list()).contains(this.receivedFilename)) {
			File file = new File(dir, this.receivedFilename);
			this.receivedFilename = null;
			return file.getName().split("[.]")[1];
		}
		return null;
	}
	
	private class MessageThread extends Thread {
		
		private Channel 			channel;
		private String 				queueName;
		
		/**
		 * Constructor for creating a MessageThread.
		 */
		private MessageThread() {
			this.channel = connection.getChannel();
			this.queueName = connection.getQueueName();
		}
		
		@Override
		public void run() {
			try {
				DeliverCallback deliverCallback = (consumerTag, delivery) -> {
					String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
					process(message);
				};
				
				channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * Process the message and set receivedFilename if user is receiving file and after file transfer completes.
		 * 
		 * @param message		The message received.
		 */
		private void process(String message) {
			ProcessMessage processMessage = new ProcessMessage(user, connection, message);
			// receivedObj contains the filename received and the running thread or null if user did not receive a file
			ReceiveObj receiveObj = processMessage.process();
			if (receiveObj != null) {
				while(receiveObj.getRunningThread().isAlive()) {
					// wait until thread is finished before setting the filename
				}
				receivedFilename = receiveObj.getFilename();
			}
		}
	}
}

