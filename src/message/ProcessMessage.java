package message;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONObject;

import constants.Constants;
import message.Wormhole.ReceiveObj;
import rabbitmq.RabbitMQConnection;
import user.User;

public class ProcessMessage {
	
	private User							user;
	private Message 						message;
	
	// received message info
	private String 							senderID;
	private String 							messageType;
	private String 							messageID;
	
	// receiving user info
	private String 							userID;
	private List<Path> 						filepaths;
	private List<String> 					wantFormats;
	private Map<String, ArrayList<String>>	convertFormats;
	
	// connection to send message
	private RabbitMQConnection 				connection;
	
	
	/**
	 * Constructor
	 * 
	 * @param user			User receiving the message.
	 * @param connection	The RabbitMQ connection for this user.
	 * @param message		Received message.
	 */
	public ProcessMessage(User user, RabbitMQConnection connection, String message) {
		this.user = user;
		JSONObject root = new JSONObject(message);
		JSONObject metadata = root.getJSONObject(Constants.METADATA);
		
		// Ignore messages current user sent
		if (!Objects.equals(metadata.getString(Constants.USER_ID), user.getUserID())) {
			this.message = new Message(message);
			
			this.senderID = this.message.getSenderID();
			this.messageType = this.message.getMessageType();
			this.messageID = this.message.getMessageID();
			
			this.userID = this.user.getUserID();
			this.filepaths = this.user.getFilepaths();
			this.wantFormats = this.user.getWantFormats();
			this.convertFormats = this.user.getConvertFormats();
			this.connection = connection;
		}
	}
	
	/**
	 * Directs to method for handling received message
	 */
	public ReceiveObj process() {
		if (this.message != null) {
			System.out.println(String.format(" [x] Received %s", this.message));
			this.user.addReceivedMessage(this.messageID, this.message);
			
			Path requestedFilepath = getFilepath();
			
			if (requestedFilepath != null) {
				hasRequestedData(requestedFilepath);
			}
		
			if (!this.wantFormats.isEmpty() && requestedFilepath == null) {
				wantsAndDoesNotHaveData();
			}
			
			if (!this.convertFormats.isEmpty()) {
				canConvertData();
			}
			
			if (Objects.equals(this.messageType, Constants.SENT_DATA)) {
				String filename = this.message.getFileData().get(0).getFileName();
				return Wormhole.receive(this.message.getContent(), filename);
			}
		}
		return null;
	}
	
	/**
	 * The user has the data in the received message.
	 * 
	 * @param filepath		The path of the file another user wants.
	 */
	private void hasRequestedData(Path filepath) {
		switch (this.messageType) {
			// a user is requesting the data
			case Constants.REQUEST_DATA:
				Wormhole.send(connection, this.userID, this.message, filepath);
				break;
			
			// user wants the data converted
			case Constants.CAN_TRANSLATE:
				wantConvertedData();
				break;
				
			default: break;
		}
	}
	
	/**
	 * The user wants the data and does not already have it.
	 */
	private void wantsAndDoesNotHaveData() {
		switch (this.messageType) {
			// if the message is a user announcing data
			case Constants.ANNOUNCE_MESSAGE:
				wantData(false);
				break;
			// if another user announces they can convert the data
			case Constants.CAN_TRANSLATE:
				wantConvertedData();
				break;

			default: break;
		}
	}
	
	/**
	 * The user can convert the data format.
	 */
	private void canConvertData() {
		switch (this.messageType) {
			case Constants.ANNOUNCE_MESSAGE:
				convertDataAnnouncement();
				break;
	
			case Constants.REQUEST_DATA:
				wantData(true);
				// TODO convert data format
				// TODO send data to user requesting
				break;
		
			default: break;
		}
	}
	
	/**
	 * Check if data is in a format the user wants.
	 * 
	 * @param forConvert		True if the user is requesting the data from the user for converting. False otherwise.
	 */
	@SuppressWarnings("unchecked")
	private void wantData(boolean forConvert) {
		
		List<String> requestWantFormats = this.wantFormats;
		List<FileData> data = this.message.getFileData();
		String requestMessageID = this.messageID;
		String originSenderID = this.senderID;
		
		// if the user is requesting the original data from the user who has it
		if (forConvert) {
			Object[] wantArgs = getWantArgs();
			for (Object wantArg : wantArgs) {
				if (wantArg == null) {
					return;
				}
			}
			requestWantFormats = (List<String>) wantArgs[0];
			data = (List<FileData>) wantArgs[1];
			requestMessageID = (String) wantArgs[2];
			originSenderID = (String) wantArgs[3];
		}
		
		for (FileData filedata : data) {
			String filename = filedata.getFileName();
			String fileformat = filename.split("[.]")[1];
			
			// if the format the sender sent is one the user wants
			if (requestWantFormats.contains(fileformat)) {
				Message requestMessage = new Message(this.userID, Constants.REQUEST_DATA);
				requestMessage.addRequestFormats(requestWantFormats);
				requestMessage.requestFile(filedata);
				requestMessage.addOriginMessageID(requestMessageID);
				requestMessage.addSourceUserID(originSenderID);
				requestMessage.addContent("Requesting file '" + filename + "'");
				this.connection.direct(requestMessage, originSenderID);
				break;
			}
		}
	}
	
	/**
	 * Helper function for wantData when a user who can translate receives a request for translation.
	 * 
	 * @return		An array with a list of specific want formats, a list of the announced data, the original message id, and the producer id.
	 */
	private Object[] getWantArgs() {
		List<String> requestFormats = this.message.getRequestFormats();
		List<String> newWantedFormats = new ArrayList<>();
		Object[] wantArgs = new Object[4];
		
		// check if the format the user is requesting the data to be translated to, is valid for the translator
		for (String format : requestFormats) {
			for (Map.Entry<String, ArrayList<String>> entry : this.convertFormats.entrySet()) {
				ArrayList<String> toFormats = entry.getValue();
				
				// if the format requested is a destination format, add the original format to newWantedFormats
				if (toFormats.contains(format)) {
					newWantedFormats.add(entry.getKey());
				}
			}
		}
		
		// get the origin message ID
		String originMessageID = this.message.getOriginMessageID();
		Message originMessage = this.user.getMessage(originMessageID);
		
		// return empty array if no origin message can be found
		if (originMessage == null) {
			return wantArgs;
		}

		wantArgs[0] = newWantedFormats;
		wantArgs[1] = originMessage.getFileData();
		wantArgs[2] = originMessageID;
		wantArgs[3] = originMessage.getSenderID();
		
		return wantArgs;
	}
	
	/**
	 * Send can translate announcement if user can translate the data.
	 */
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
		
		Message requestMessage = new Message(this.userID, Constants.CAN_TRANSLATE);
		
		for (FileData file : requestData) {
			requestMessage.requestFile(file);
		}
		
		for (Map.Entry<String, ArrayList<String>> entry : convertableFormats.entrySet()) {
			String originalFormat = entry.getKey();
			for (String destFormat : convertableFormats.get(originalFormat)) {
				requestMessage.addConvertFormat(originalFormat, destFormat);
			}
		}
		
		requestMessage.addOriginMessageID(this.messageID);
		requestMessage.addSourceUserID(this.senderID);
		requestMessage.addContent("I can convert the data from " + convertableFormats);
		this.connection.announce(requestMessage);
	}
	
	/**
	 * Send request to translator for file data converted.
	 */
	private void wantConvertedData() {
		Map<String, ArrayList<String>> announcedConvertableFormats = this.message.getConvertFormats();
		boolean foundFormat = false;
		String requestFormat = null;
		
		for (String want : this.wantFormats) {
			for (ArrayList<String> destFormat : announcedConvertableFormats.values()) {
				if (destFormat.contains(want)) {
					requestFormat = want;
					foundFormat = true;
					break;
				}
			}
			if (foundFormat) {
				break;
			}
		}
		
		if (requestFormat != null) {
			Message requestMessage = new Message(this.userID, Constants.REQUEST_DATA);
			for (FileData file : this.message.getFileData()) {
				requestMessage.requestFile(file);
			}
			requestMessage.addRequestFormats(requestFormat);
			requestMessage.addOriginMessageID(this.message.getOriginMessageID());
			requestMessage.addSourceUserID(this.message.getSourceUserID());
			requestMessage.addContent("Requesting data to be converted to " + requestFormat);
			this.connection.direct(requestMessage, this.senderID);
		}
	}
	
	/**
	 * Get the file path corresponding of the requested file.
	 * 
	 * @return		The path of the file wanted or null if not found.
	 */
	private Path getFilepath() {
		for (Path path : this.filepaths) {
			String filename = path.getFileName().toString();
			for (FileData data : this.message.getFileData()) {
				if (Objects.equals(data.getFileName(), filename)) {
					return path;
				}
			}
		}
		
		return null;
	}

}
