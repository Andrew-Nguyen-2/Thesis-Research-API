package message;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import constants.Constants;

/**
 * Used to generate messages for sending and to convert received messages.
 * 
 * @author andrewnguyen
 *
 */
public class Message {

	private Metadata metadata;
	private String content;

	// ****************************************
	//
	// generate a Message instance for sending
	//
	// ****************************************

	/**
	 * Constructor for Message when sending.
	 * 
	 * @param userID      The ID of the user sending the message.
	 * @param messageType The message type for the message.
	 */
	public Message(String userID, String messageType) {
		this.metadata = new Metadata(userID, messageType);
	}

	/**
	 * Add the file path to the metadata instance.
	 * 
	 * @param filepath The file path of the file to be shared.
	 */
	public void addFilePath(String filepath) {
		try {
			Path validPath = Paths.get(filepath);
			metadata.setData(validPath);

		} catch (InvalidPathException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add the FileData when requesting data.
	 * 
	 * @param filedata The file data the user is requesting.
	 */
	public void requestFile(FileData filedata) {
		metadata.setData(filedata);
	}

	/**
	 * Add the data formats the user wants.
	 * 
	 * @param wants The formats the user wants.
	 */
	public void addRequestFormats(String... wants) {
		metadata.setDataRequestFormats(wants);
	}

	/**
	 * Add the data formats the user wants (from User instance).
	 * 
	 * @param wants The list of formats the user wants.
	 */
	public void addRequestFormats(List<String> wants) {
		for (String want : wants) {
			metadata.setDataRequestFormats(want);
		}
	}

	/**
	 * Add the format the user can convert from and to.
	 * 
	 * @param originalFormat    The original format of the file data in the
	 *                          announced message.
	 * @param destinationFormat The format it will be converted to.
	 */
	public void addConvertFormat(String originalFormat, String destinationFormat) {
		metadata.setDataConvertFormats(originalFormat, destinationFormat);
	}

	/**
	 * Add the origin message ID the message is referring to.
	 * 
	 * @param messageID The origin message ID.
	 */
	public void addOriginMessageID(String messageID) {
		metadata.setOriginMessageID(messageID);
	}

	/**
	 * Add the user ID of the original sender the message is referring to.
	 * 
	 * @param sourceUserID The origin (source) user ID.
	 */
	public void addSourceUserID(String sourceUserID) {
		metadata.setSourceUserID(sourceUserID);
	}

	/**
	 * Add the content for the message.
	 * 
	 * @param content
	 */
	public void addContent(String content) {
		this.content = content;
	}

	/**
	 * Convert the Message instance to a JSON string.
	 * 
	 * @return The message as a JSON string.
	 */
	public String toJSON() {
		JSONObject message = new JSONObject();
		message.put(Constants.METADATA, metadata.toJSON());
		message.put(Constants.CONTENT, content);
		return message.toString();
	}

	// ****************************************
	//
	// generate a Message instance from JSON
	//
	// ****************************************

	/**
	 * Constructor for loading the received JSON string into a Message instance.
	 * 
	 * @param message The message received as a string.
	 */
	public Message(String message) {
		JSONObject root = new JSONObject(message);

		JSONObject metadataJSON = root.getJSONObject(Constants.METADATA);
		metadata = new Metadata(metadataJSON);
		content = root.getString(Constants.CONTENT);
	}

	/**
	 * Get the ID of the user who sent the message.
	 * 
	 * @return The ID of the sender.
	 */
	public String getSenderID() {
		return metadata.userID;
	}

	/**
	 * Get the type of message received.
	 * 
	 * @return The message type of the message.
	 */
	public String getMessageType() {
		return metadata.messageType;
	}

	/**
	 * Get the ID of the message received.
	 * 
	 * @return The received message's ID.
	 */
	public String getMessageID() {
		return metadata.messageID;
	}

	/**
	 * Get the list of file data in the received message.
	 * 
	 * @return List of FileData instances if they exist.
	 */
	public List<FileData> getFileData() {
		return metadata.data;
	}

	/**
	 * Get the list of requested formats in the received message.
	 * 
	 * @return List of formats if they exist.
	 */
	public List<String> getRequestFormats() {
		return metadata.dataRequestFormats;
	}

	/**
	 * Get the formats that can be converted from and to.
	 * 
	 * @return A map of the formats that can be converted from and a list of formats
	 *         they can be converted to.
	 */
	public Map<String, ArrayList<String>> getConvertFormats() {
		return metadata.dataConvertFormats;
	}

	/**
	 * Get the origin message ID the message is referring to.
	 * 
	 * @return The origin message ID.
	 */
	public String getOriginMessageID() {
		return metadata.originMessageID;
	}

	/**
	 * Get the origin user ID the message is referring to.
	 * 
	 * @return The origin (source) user ID.
	 */
	public String getSourceUserID() {
		return metadata.sourceUserID;
	}

	/**
	 * Print the Message instance as "metadata = " followed by the metadata and
	 * "content = " followed by the message content.
	 */
	public String toString() {
		return String.format("metadata = %scontent = %s%n", metadata, content);
	}

	/**
	 * Getter for metadata.
	 * 
	 * @return Metadata instance for the metadata of the message.
	 */
	public Metadata getMedata() {
		return this.metadata;
	}

	/**
	 * Getter for content.
	 * 
	 * @return Content string of the message.
	 */
	public String getContent() {
		return this.content;
	}
}
