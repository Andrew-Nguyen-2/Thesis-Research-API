package message;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import constants.Constants;

/**
 * Used to generate messages for sending and to convert received messages.
 * @author andrewnguyen
 *
 */
public class Message {
	
	private Metadata 	metadata;
	private String 		content;
	
	// ****************************************
	//
	// generate a Message instance for sending
	//
	// ****************************************
	
	/**
	 * Constructor for Message when sending.
	 * 
	 * @param userID		The ID of the user sending the message.
	 * @param messageType	The message type for the message.
	 */
	public Message(String userID, String messageType) {
		this.metadata = new Metadata(userID, messageType);
	}
	
	/**
	 * Add the file path to the metadata instance.
	 * 
	 * @param filepath		The file path of the file to be shared.
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
	 * @param filedata		The file data the user is requesting.
	 */
	public void requestFile(FileData filedata) {
		metadata.setData(filedata);
	}
	
	
	/**
	 * Add the data formats the user wants.
	 * 
	 * @param wants		The formats the user wants.
	 */
	public void addRequestFormats(String...wants) {
		metadata.setDataRequestFormats(wants);
	}
	
	/**
	 * Add the data formats the user wants (from User instance).
	 * 
	 * @param wants		The list of formats the user wants.
	 */
	public void addRequestFormats(List<String> wants) {
		for (String want : wants) {
			metadata.setDataRequestFormats(want);
		}
	}
	
	/**
	 * Add the format the user can convert from and to.
	 * 
	 * @param originalFormat		The original format of the file data in the announced message.
	 * @param destinationFormat		The format it will be converted to.
	 */
	public void addConvertFormat(String originalFormat, String destinationFormat) {
		metadata.setDataConvertFormats(originalFormat, destinationFormat);
	}
	
	/**
	 * Add the origin message ID the message is referring to.
	 * 
	 * @param messageID		The origin message ID.
	 */
	public void addOriginMessageID(String messageID) {
		metadata.setOriginMessageID(messageID);
	}
	
	/**
	 * Add the user ID of the original sender the message is referring to.
	 * 
	 * @param sourceUserID		The origin (source) user ID.
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
	 * @return		The message as a JSON string.
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
	 * @param message		The message received as a string.
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
	 * @return		The ID of the sender.
	 */
	public String getSenderID() {
		return metadata.userID;
	}
	
	/**
	 * Get the type of message received.
	 * 
	 * @return		The message type of the message.
	 */
	public String getMessageType() {
		return metadata.messageType;
	}
	
	/**
	 * Get the ID of the message received.
	 * 
	 * @return		The received message's ID.
	 */
	public String getMessageID() {
		return metadata.messageID;
	}
	
	/**
	 * Get the list of file data in the received message.
	 * 
	 * @return		List of FileData instances if they exist.
	 */
	public List<FileData> getFileData() {
		return metadata.data;
	}
	
	/**
	 * Get the list of requested formats in the received message.
	 * 
	 * @return		List of formats if they exist.
	 */
	public List<String> getRequestFormats() {
		return metadata.dataRequestFormats;
	}
	
	/**
	 * Get the formats that can be converted from and to.
	 * 
	 * @return		A map of the formats that can be converted from and a list of formats they can be converted to.
	 */
	public Map<String, ArrayList<String>> getConvertFormats() {
		return metadata.dataConvertFormats;
	}
	
	/**
	 * Get the origin message ID the message is referring to.
	 * 
	 * @return		The origin message ID.
	 */
	public String getOriginMessageID() {
		return metadata.originMessageID;
	}
	
	/**
	 * Get the origin user ID the message is referring to.
	 * 
	 * @return		The origin (source) user ID.
	 */
	public String getSourceUserID() {
		return metadata.sourceUserID;
	}
	
	/**
	 * Print the Message instance as "metadata = " followed by the metadata and "content = " followed by the message content.
	 */
	public String toString() {
		return String.format("metadata = %scontent = %s%n", metadata, content);
	}

	/**
	 * Getter for metadata.
	 * 
	 * @return		Metadata instance for the metadata of the message.
	 */
	public Metadata getMedata() {
		return this.metadata;
	}
	
	/**
	 * Getter for content.
	 * 
	 * @return		Content string of the message.
	 */
	public String getContent() {
		return this.content;
	}
	
	private class Metadata {
		private String 							userID;
		
		private String 							messageType;
		private String 							messageID;
		
		private ArrayList<FileData> 			data					= new ArrayList<>();
		
		private ArrayList<String> 				dataRequestFormats		= new ArrayList<>();
		private Map<String, ArrayList<String>>	dataConvertFormats		= new HashMap<>();
		
		private String 							originMessageID			= "";
		private String 							sourceUserID			= "";
		
		private String							timestamp;
		
		
		// ****************************************
		//
		// generate Metadata instance for sending
		//
		// ****************************************
		
		/**
		 * Constructor for Metadata when sending.
		 * 
		 * @param userID			The ID of the user sending the message.
		 * @param messageType		The message type for the message.
		 */
		private Metadata(String userID, String messageType) {
			this.userID = userID;
			this.messageType = messageType;
			this.messageID = UUID.randomUUID().toString();
			this.timestamp = Instant.now().toString();
		}
		
		/**
		 * Add the file path to the array list of FileData instances.
		 * 
		 * @param filepath			The file path of the file shared.
		 * @throws IOException
		 */
		private void setData(Path filepath) throws IOException {
			String filename = filepath.getFileName().toString();
			String filesize = String.valueOf(Files.size(filepath));
			data.add(new FileData(filename, filesize));
		}
		
		/**
		 * Add the requested file data to the metadata.
		 * 
		 * @param filedata		The file data the user is requesting.
		 */
		private void setData(FileData filedata) {
			data.add(filedata);
		}
		
		/**
		 * Add the request formats.
		 * 
		 * @param wants		The formats the user wants
		 */
		private void setDataRequestFormats(String...wants) {
			this.dataRequestFormats.addAll(Arrays.asList(wants));
		}
		
		/**
		 * Add the original format to the map and destination format to the respected list.
		 * 
		 * @param original			The original format.
		 * @param destination		The destination format.
		 */
		private void setDataConvertFormats(String original, String destination) {
			dataConvertFormats.putIfAbsent(original, new ArrayList<>());
			dataConvertFormats.get(original).add(destination);
		}
		
		/**
		 * Set the origin message ID.
		 * 
		 * @param originMessageID
		 */
		private void setOriginMessageID(String originMessageID) {
			this.originMessageID = originMessageID;
		}
		
		/**
		 * Set the source user ID.
		 * 
		 * @param sourceUserID
		 */
		private void setSourceUserID(String sourceUserID) {
			this.sourceUserID = sourceUserID;
		}
		
		/**
		 * Convert the Metadata instance to a JSONObject.
		 * 
		 * @return		JSONObject.
		 */
		private JSONObject toJSON() {
			JSONObject meta = new JSONObject();
			meta.put(Constants.USER_ID, userID);
			meta.put(Constants.MESSAGE_TYPE, messageType);
			meta.put(Constants.MESSAGE_ID, messageID);
			meta.put(Constants.METADATA_FILEDATA, dataToJSON());
			meta.put(Constants.DATA_CONVERT_FORMATS, convertFormatsToJSON());
			meta.put(Constants.DATA_REQUEST_FORMATS, requestFormatsToJSON());
			meta.put(Constants.ORIGIN_MESSAGE_ID, originMessageID);
			meta.put(Constants.SOURCE_USER_ID, sourceUserID);
			meta.put(Constants.TIMESTAMP, timestamp);
			return meta;
		}
		
		/**
		 * Convert the array list of FileData instances to a JSONObject.
		 * 
		 * @return		JSONArray.
		 */
		private JSONArray dataToJSON() {
			JSONArray filedata = new JSONArray();
			
			for (FileData file : this.data) {
				JSONObject currFileData = new JSONObject();
				currFileData.put(Constants.FILENAME, file.getFileName());
				currFileData.put(Constants.FILESIZE, file.getFileSize());
				filedata.put(currFileData);
			}
			return filedata;
		}
		
		/**
		 * Convert the map of original formats and their respected array list of destination formats to a JSONObject.
		 * 
		 * @return		JSONArray.
		 */
		private JSONArray convertFormatsToJSON() {
			JSONArray convert = new JSONArray();
			
			for (Map.Entry<String, ArrayList<String>> entry : this.dataConvertFormats.entrySet()) {
				JSONObject currConvert = new JSONObject();
				JSONArray destinationFormats = new JSONArray();
				for (String destFormat : entry.getValue()) {
					destinationFormats.put(destFormat);
				}
				currConvert.put(Constants.ORIGINAL_FORMAT, entry.getKey());
				currConvert.put(Constants.DESTINATION_FORMATS, destinationFormats);
				convert.put(currConvert);
			}
			return convert;
		}
		
		/**
		 * Convert the array list of requests formats to a JSONArray.
		 * 
		 * @return		JSONArray.
		 */
		private JSONArray requestFormatsToJSON() {
			JSONArray requests = new JSONArray();
			
			for (String request : dataRequestFormats) {
				requests.put(request);
			}
			return requests;
		}
		

		// ****************************************
		//
		// generate Metadata instance from JSON
		//
		// ****************************************
		
		/**
		 * Constructor for Metadata instance when receiving.
		 * 
		 * @param metadataJSONObj		The metadata as a JSONObject.
		 */
		private Metadata(JSONObject metadataJSONObj) {
			userID = metadataJSONObj.getString(Constants.USER_ID);
			messageType = metadataJSONObj.getString(Constants.MESSAGE_TYPE);
			messageID = metadataJSONObj.getString(Constants.MESSAGE_ID);
			setData(metadataJSONObj.getJSONArray(Constants.METADATA_FILEDATA));
			setDataRequestFormats(metadataJSONObj.getJSONArray(Constants.DATA_REQUEST_FORMATS));
			setDataConvertFormats(metadataJSONObj.getJSONArray(Constants.DATA_CONVERT_FORMATS));
			originMessageID = metadataJSONObj.getString(Constants.ORIGIN_MESSAGE_ID);
			sourceUserID = metadataJSONObj.getString(Constants.SOURCE_USER_ID);
			timestamp = metadataJSONObj.getString(Constants.TIMESTAMP);
		}
		
		/**
		 * Set the data after parsing JSONArray containing the file data.
		 * 
		 * @param filedata		The JSONArray containing the filename and file size.
		 */
		private void setData(JSONArray filedata) {
			for (Object file : filedata) {
				String filename = ((JSONObject) file).getString(Constants.FILENAME);
//				String filesize = Integer.toString(((JSONObject) file).getInt(Constants.FILESIZE));
				String filesize = ((JSONObject) file).get(Constants.FILESIZE).toString();
				this.data.add(new FileData(filename, filesize));
			}
			
		}
		
		/**
		 * Set the data request formats after parsing JSONArray containing the formats.
		 * 
		 * @param dataRequestFormats		The JSONArray containing request formats.
		 */
		private void setDataRequestFormats(JSONArray dataRequestFormats) {
			for (Object requestFormat : dataRequestFormats) {
				this.dataRequestFormats.add(String.valueOf(requestFormat));
			}
		}
		
		/**
		 * Set the data convert formats after parsing the JSONArray containing the formats.
		 * 
		 * @param dataConvertFormats		The JSONArray containing the convert formats.
		 */
		private void setDataConvertFormats(JSONArray dataConvertFormats) {
			for (Object convert : dataConvertFormats) {
				ArrayList<String> destFormats = new ArrayList<>();
				for (Object toFormat: ((JSONObject) convert).getJSONArray(Constants.DESTINATION_FORMATS)) {
					destFormats.add(toFormat.toString());
				}
				this.dataConvertFormats.put(((JSONObject) convert).getString(Constants.ORIGINAL_FORMAT), destFormats);
			}
		}
		
		/**
		 * Print the Metadata instance as:
		 * user_id: "", message_id: "", message_type: ""
		 * data: [], data_convert_formats: {}, data_request_formats: []
		 * timestamp: "", origin_message_id: "", source_user_id: ""
		 */
		public String toString() {
			String metadataString = String.format("user_id: %s, message_id: %s, message_type: %s%n", userID, messageID, messageType);
			metadataString += String.format("data: %s, data_convert_formats: %s, data_request_formats: %s%n", data, dataConvertFormats, dataRequestFormats);
			metadataString += String.format("timestamp: %s, origin_message_id: %s, source_user_id: %s%n", timestamp, originMessageID, sourceUserID);
			
			return metadataString;
		}
	}
}
