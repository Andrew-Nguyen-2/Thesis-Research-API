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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import constants.Constants;

public class Message {
	
	private Metadata metadata;
	private String 	content;
	
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
	 * Add the data formats the user wants.
	 * 
	 * @param wants		The formats the user wants.
	 */
	public void addRequestFormats(String...wants) {
		metadata.setDataRequestFormats(wants);
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
	 * Convert the Message instance to a JSON string.
	 * 
	 * @return		The message as a JSON string.
	 */
	public String toJSON() {
		JSONObject message = new JSONObject();
		message.put("metadata", metadata.toJSON());
		message.put("content", content);
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
		
		JSONObject metadataJSON = root.getJSONObject("metadata");
		String userID = metadataJSON.getString("user_id");
		metadata = new Metadata(metadataJSON);
		content = root.getString("content");
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
	public String content() {
		return this.content;
	}
	
	private class Metadata {
		private String userID;
		
		private String messageType;
		private String messageID;
		
		private ArrayList<FileData> 			data;
		
		private ArrayList<String> 				dataRequestFormats;
		private Map<String, ArrayList<String>>	dataConvertFormats;
		
		private String 							originMessageID;
		private String 							sourceUserID;
		
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
			
			this.data = new ArrayList<>();
			this.dataRequestFormats = new ArrayList<>();
			this.dataConvertFormats = new HashMap<>();
		}
		
		/**
		 * Add the file path to the array list of FileData instances
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
		 * @return		JSONObject.
		 */
		private JSONObject dataToJSON() {
			JSONObject filedata = new JSONObject();
			
			for (FileData file : this.data) {
				filedata.put(file.filename, file.filesize);
			}
			return filedata;
		}
		
		/**
		 * Convert the map of original formats and their respected array list of destination formats to a JSONObject.
		 * 
		 * @return		JSONObject.
		 */
		private JSONObject convertFormatsToJSON() {
			JSONObject convert = new JSONObject();
			
			for (Map.Entry<String, ArrayList<String>> entry : this.dataConvertFormats.entrySet()) {
				JSONArray destinationFormats = new JSONArray();
				for (String destFormat : entry.getValue()) {
					destinationFormats.put(destFormat);
				}
				convert.put(entry.getKey(), destinationFormats);
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
			userID = metadataJSONObj.getString("user_id");
			messageType = metadataJSONObj.getString("message_type");
			messageID = metadataJSONObj.getString("message_type");
			setData(metadataJSONObj.getJSONObject("data"));
			setDataRequestFormats(metadataJSONObj.getJSONArray("data_request_formats"));
			setDataConvertFormats(metadataJSONObj.getJSONObject("data_convert_formats"));
			originMessageID = metadataJSONObj.getString("origin_message_id");
			sourceUserID = metadataJSONObj.getString("source_user_id");
			timestamp = metadataJSONObj.getString("time_stamp");
		}
		
		/**
		 * Set the data after parsing JSONObject containing the file data.
		 * 
		 * @param filedata		The JSONObject containing the filename and file size.
		 */
		private void setData(JSONObject filedata) {
			Iterator<String> keys = filedata.keys();
			
			
			while (keys.hasNext()) {
				String filename = keys.next();
				String filesize = Integer.toString(filedata.getInt(filename));
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
		 * Set the data convert formats after parsing the JSONObject containing the formats.
		 * 
		 * @param dataConvertFormats		The JSONObject containing the convert formats.
		 */
		private void setDataConvertFormats(JSONObject dataConvertFormats) {
			Iterator<String> keys = dataConvertFormats.keys();
			
			while (keys.hasNext()) {
				String originalFormat = keys.next();
				JSONArray destFormats = dataConvertFormats.getJSONArray(originalFormat);
				ArrayList<String> toFormats = new ArrayList<>();
				for (Object toFormat: destFormats) {
					toFormats.add(String.valueOf(toFormat));
				}
				this.dataConvertFormats.put(originalFormat, toFormats);
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
