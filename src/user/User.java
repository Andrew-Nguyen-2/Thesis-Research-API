package user;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import message.Message;

/**
 * Create a user and store their information.
 * 
 * @author andrewnguyen
 *
 */
public class User {
	private String userID;

	private ArrayList<String> want;
	private Map<String, ArrayList<String>> convert;
	private ArrayList<Path> filepaths;

	private Map<String, Message> receivedMessages; // Key: MessageID, Value: Message
	private Map<String, ArrayList<String>> filesRequested; // Key: SourceUserID, Value: [Filenames]
	private Map<String, ArrayList<String>> translationsRequested; // Key: Filename, Value: [DestinationFormats]
	private Message requestMessage;

	/**
	 * Constructor for creating a User instance.
	 */
	public User() {
		this.userID = UUID.randomUUID().toString();
		this.want = new ArrayList<>();
		this.convert = new HashMap<>();
		this.filepaths = new ArrayList<>();
		this.receivedMessages = new HashMap<>();
		this.filesRequested = new HashMap<>();
		this.translationsRequested = new HashMap<>();
	}

	// ************************************
	//
	// Setters
	//
	// ************************************

	/**
	 * Add the formats the user wants.
	 * 
	 * @param wantFormats The formats the user wants.
	 */
	public void addWant(String... wantFormats) {
		this.want.addAll(Arrays.asList(wantFormats));
	}

	/**
	 * Add the formats the user can translate the data from and to.
	 * 
	 * @param original    The original data format.
	 * @param destination The translated data format.
	 */
	public void addConvert(String original, String destination) {
		this.convert.putIfAbsent(original, new ArrayList<>());
		this.convert.get(original).add(destination);
	}

	/**
	 * Add the file path of the file the user wants to share.
	 * 
	 * @param filepath The file path of the file.
	 */
	public void addFilepaths(String filepath) {
		Path validPath = Paths.get(filepath);
		this.filepaths.add(validPath);
	}

	/**
	 * Add the received message to user history.
	 * 
	 * @param messageID The ID of the received message.
	 * @param message   The Message instance of the received message.
	 */
	public void addReceivedMessage(String messageID, Message message) {
		this.receivedMessages.put(messageID, message);
	}

	/**
	 * Add a file request to sourceUserID mapping in requests.
	 * 
	 * @param sourceUserID The user who has the data.
	 * @param filename     The name of the file requested.
	 */
	public void addFileRequest(String sourceUserID, String filename) {
		this.filesRequested.putIfAbsent(sourceUserID, new ArrayList<>());
		this.filesRequested.get(sourceUserID).add(filename);
	}

	/**
	 * Remove the file request for the sender and delete sender from hash map if no
	 * more requests.
	 * 
	 * @param sourceUserID The user who has the data.
	 * @param filename     The name of the file to be removed from requests.
	 */
	public void removeFileRequest(String sourceUserID, String filename) {
		this.filesRequested.get(sourceUserID).remove(filename);
		if (this.filesRequested.get(sourceUserID).isEmpty()) {
			this.filesRequested.remove(sourceUserID);
		}
	}

	public void addRequestMessage(Message message) {
		this.requestMessage = message;
	}

	public void removeRequestMessage() {
		this.requestMessage = null;
	}

	/**
	 * Add a translation request to filename mapping.
	 * 
	 * @param filename          The filename being requested for translation.
	 * @param destinationFormat The requested format for translation.
	 */
	public void addTranslationRequest(String filename, String destinationFormat) {
		this.translationsRequested.putIfAbsent(filename, new ArrayList<>());
		this.translationsRequested.get(filename).add(destinationFormat);
	}

	/**
	 * Remove the translation request for the filename and remove filename from hash
	 * map if no more destination formats.
	 * 
	 * @param filename          The filename requested for translation.
	 * @param destinationFormat The requested format for translation.
	 */
	public void removeTranslationRequest(String filename, String destinationFormat) {
		if (this.translationsRequested.get(filename) == null) {
			return;
		}
		this.translationsRequested.get(filename).remove(destinationFormat);
		if (this.translationsRequested.get(filename).isEmpty()) {
			this.translationsRequested.remove(filename);
		}
	}

	// ************************************
	//
	// Getters
	//
	// ************************************

	/**
	 * Get the formats the user wants.
	 * 
	 * @return A list of the formats the user wants.
	 */
	public List<String> getWantFormats() {
		return this.want;
	}

	/**
	 * Get all the formats the user can translate.
	 * 
	 * @return A Map of the formats the user can translate from and a
	 *         list of the formats they can translate the specific format to.
	 */
	public Map<String, ArrayList<String>> getConvertFormats() {
		return this.convert;
	}

	/**
	 * Get the formats the user can translate to from a specific format;
	 * 
	 * @param originalFormat The original format the user can translate from.
	 * @return A list of the formats the user can translate to.
	 */
	public List<String> getDestinationFormats(String originalFormat) {
		return this.convert.get(originalFormat);
	}

	/**
	 * Get the file paths the user has added.
	 * 
	 * @return A list of the file paths the user has.
	 */
	public List<Path> getFilepaths() {
		return this.filepaths;
	}

	/**
	 * Get all the messages the user has received.
	 * 
	 * @return A map of the message ID's and relating Message instances.
	 */
	public Map<String, Message> getAllMessages() {
		return this.receivedMessages;
	}

	/**
	 * Get a specific message the user has received.
	 * 
	 * @param messageID The ID of the message wanted.
	 * @return A Message instance of the message with the message ID.
	 */
	public Message getMessage(String messageID) {
		return this.receivedMessages.get(messageID);
	}

	/**
	 * Get the ID of the user.
	 * 
	 * @return The ID of the user.
	 */
	public String getUserID() {
		return userID;
	}

	/**
	 * Get the list of files requested from a user.
	 * 
	 * @param sourceUserID The user who has data.
	 * @return An ArrayList of filenames requested for the user.
	 */
	public List<String> getFilesRequested(String sourceUserID) {
		return this.filesRequested.get(sourceUserID);
	}

	public Message getRequestMessage() {
		return this.requestMessage;
	}

	/**
	 * Get the list of destination formats requested for a filename.
	 * 
	 * @param filename The filename requested for translation.
	 * @return An ArrayList of destination formats requested by the user.
	 */
	public List<String> getTranslationFormatRequests(String filename) {
		return this.translationsRequested.get(filename);
	}
}
