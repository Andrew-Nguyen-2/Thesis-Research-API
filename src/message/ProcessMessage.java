package message;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import constants.Constants;
import rabbitmq.RabbitMQConnection;
import user.User;

public class ProcessMessage {
	
	private User user;
	private Message message;
	
	// received message info
	private String senderID;
	private String messageType;
	
	// receiving user info
	private String userID;
	private List<Path> filepaths;
	private List<String> wantFormats;
	private Map<String, ArrayList<String>> convertFormats;
	
	// connection to send message
	private RabbitMQConnection connection;
	
	
	/**
	 * Constructor
	 * 
	 * @param user			User receiving the message.
	 * @param message		Received message.
	 */
	public ProcessMessage(User user, String message) {
		this.user = user;
		this.message = new Message(message);
		
		this.senderID = this.message.getSenderID();
		this.messageType = this.message.getMessageType();
		
		this.userID = this.user.getUserID();
		this.filepaths = this.user.getFilepaths();
		this.wantFormats = this.user.getWantFormats();
		this.convertFormats = this.user.getConvertFormats();
		
		try {
			this.connection = new RabbitMQConnection(user);
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Directs to method for handling received message
	 */
	public void process() {
		
		// Ignore messages current user sent
		if (!Objects.equals(senderID, userID)) {
			System.out.println(String.format(" [x] Received %s", message));
			user.addReceivedMessage(message.getMessageID(), message);
			
			// if the user wants certain data formats
			if (!this.wantFormats.isEmpty()) {
				switch (messageType) {
				
				// if the message is a user announcing data
				case Constants.ANNOUNCE_MESSAGE:
					wantData();
					break;
				
				case Constants.REQUEST_DATA:
					break;
					
				default: break;
				
				}
			}
			
			if (!this.convertFormats.isEmpty()) {
				switch (messageType) {
				
				case Constants.ANNOUNCE_MESSAGE:
					convertDataAnnouncement();
					break;
				}
			}
			
//			if (Objects.equals(messageType, Constants.SENT_DATA)) {
//				try {
//					Wormhole.receive(message.getContent());
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
		}
	}
	
	/**
	 * Check if data is in a format the user wants.
	 */
	private void wantData() {
		List<FileData> data = message.getFileData();
		
		for (FileData filedata : data) {
			String filename = filedata.getFileName();
			String fileformat = filename.split("[.]")[1];
			
			// if the format the sender sent is one the user wants
			if (wantFormats.contains(fileformat)) {
				Message requestMessage = new Message(userID, Constants.REQUEST_DATA);
				requestMessage.addRequestFormats(wantFormats);
				requestMessage.requestFile(filedata);
				requestMessage.addOriginMessageID(message.getMessageID());
				requestMessage.addSourceUserID(message.getSenderID());
				requestMessage.addContent("Requesting file '" + filename + "'");
				connection.direct(requestMessage, message.getSenderID());
			}
		}
	}
	
	private void convertDataAnnouncement() {
		List<FileData> announcedData = this.message.getFileData();
		List<FileData> requestData = new ArrayList<>();
		Map<String, ArrayList<String>> convertableFormats = new HashMap<>();
		
		for (FileData file : announcedData) {
			String fileformat = file.getFileName().split("[.]")[1];
			if (this.convertFormats.containsKey(fileformat)) {
				requestData.add(file);
				convertableFormats.put(fileformat, this.convertFormats.get(fileformat));
			}
		}
		
		Message requestMessage = new Message(userID, Constants.CAN_TRANSLATE);
		
		for (FileData file : requestData) {
			requestMessage.requestFile(file);
		}
		
		for (Map.Entry<String, ArrayList<String>> entry : convertableFormats.entrySet()) {
			String originalFormat = entry.getKey();
			for (String destFormat : convertableFormats.get(originalFormat)) {
				requestMessage.addConvertFormat(originalFormat, destFormat);
			}
		}
		
		requestMessage.addOriginMessageID(this.message.getOriginMessageID());
		requestMessage.addSourceUserID(message.getSenderID());
		requestMessage.addContent("I can convert the data from " + convertableFormats);
		this.connection.direct(requestMessage, this.senderID);
	}

}
