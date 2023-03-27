package api;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import constants.Constants;
import logging.Log;
import message.Message;
import message.ProcessMessage;
import message.Wormhole.ReceiveObj;
import rabbitmq.RabbitMQConnection;
import user.User;

/**
 * The entry point into the api.
 */
public class ResearchAPI {

	private User user;
	private RabbitMQConnection connection;

	private String receivedFilename;

	/**
	 * Constructor for creating a ResearchAPI instance.
	 * 
	 * @param logType  The type of logging output (file or console)
	 * @param logLevel The level of logging (ex. FINE, INFO, WARNING). <br>
	 *                 Logs will be generated for levels following the one inputed.
	 *                 <br>
	 *                 For example, if logLevel is set to FINE, logs will be
	 *                 generated for INFO and WARNING.
	 */
	public ResearchAPI(String logType, String logLevel) {
		Log.setOutput(logType, logLevel);
		this.user = new User();
	}

	/**
	 * Add the formats wanted.
	 * 
	 * @param wantFormats Formats wanted.
	 */
	public void addWantFormats(String... wantFormats) {
		this.user.addWant(wantFormats);
	}

	/**
	 * Add the file path of data to share and make an announcement.
	 * 
	 * @param filepath The full file path of the data.
	 */
	public void addFile(String filepath) {
		File file = new File(filepath);
		if (!file.exists()) {
			Log.error("filepath does not exist for: '" + filepath + "'", "addFile");
			return;
		}
		this.user.addFilepaths(filepath);
		if (this.connection == null) {
			connect();
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
	 * @param originalFormat    The original data format.
	 * @param destinationFormat The translated data format.
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
		} catch (IOException | TimeoutException | KeyManagementException | NoSuchAlgorithmException
				| URISyntaxException e) {
			Log.error(e.getMessage(), ResearchAPI.class.getName() + ":" + "connect");
		}
	}

	/**
	 * Start listening for messages.
	 */
	public void startListening() {
		(new MessageThread()).start();
		Log.other(" [*] Begin listening to RabbitMQ server.");
	}

	/**
	 * Get the received file path and format.
	 * Place method call in a while true loop to continuously check if file has been
	 * received.
	 * 
	 * <pre>
	 * {@code}
	 * ResearchAPI example = new ResearchAPI();
	 * 
	 * while (true) {
	 * 	String[] fileInfo = example.getReceivedFile();
	 * 	String receivedFilePath = fileInfo[0];
	 * 	String receivedFileFormat = fileInfo[1];
	 * }
	 * </pre>
	 * 
	 * @return An array: [filePath, fileFormat] or [null, null].
	 */
	public String[] getReceivedFile() {
		String cwd = System.getProperty("user.dir");
		File dir = new File(cwd, "received-files");
		// check receivedFilename is set, received files directory exists, and filename
		// exists within the directory
		if (this.receivedFilename != null && dir.exists()
				&& Arrays.asList(dir.list()).contains(this.receivedFilename)) {
			File file = new File(dir, this.receivedFilename);
			this.receivedFilename = null;
			return new String[] { file.toString(), file.getName().split("[.]")[1] };
		}
		return new String[2];
	}

	private class MessageThread extends Thread {

		private Channel channel;
		private String queueName;

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

				channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
				});
			} catch (IOException e) {
				Log.error(e.getMessage(), MessageThread.class.getName());
			}
		}

		/**
		 * Process the message and set receivedFilename if user is receiving file and
		 * after file transfer completes.
		 * 
		 * @param message The message received.
		 */
		private void process(String message) {
			ProcessMessage processMessage = new ProcessMessage(user, connection, message);
			// receivedObj contains the filename received and the running thread or null if
			// user did not receive a file
			ReceiveObj receiveObj = processMessage.process();
			if (receiveObj != null) {
				while (receiveObj.getRunningThread().isAlive()) {
					// wait until thread is finished before setting the filename
				}
				receivedFilename = receiveObj.getNewFilename();
				Log.received("Received file: " + receivedFilename);
				user.removeFileRequest(receiveObj.getSourceUserID(), receiveObj.getOriginalFilename());
				user.removeRequestMessage();
				user.removeTranslationRequest(receiveObj.getOriginalFilename(), receiveObj.getFileFormat());
			}
		}
	}
}
