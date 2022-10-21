package research;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

public class Message {
	public Metadata metadata;
	public String 	content;
	
	// generate a Message instance for sending
	public Message(String userID, String messageType) {
		this.metadata = new Metadata(userID, messageType);
	}
	
	public void addFilePath(String filepath) {
		try {
			Path validPath = Paths.get(filepath);
			metadata.setData(validPath);
			
		} catch (InvalidPathException | IOException e) {
			System.out.println(e);
		}
	}
	
	public void setRequestFormats(String...wants) {
		metadata.setDataRequestFormats(wants);
	}
	
	public void setConvertFormat(String originalFormat, String destinationFormat) {
		metadata.setDataConvertFormats(originalFormat, destinationFormat);
	}
	
	public void setOriginMessageID(String messageID) {
		metadata.setOriginMessageID(messageID);
	}
	
	public void setSourceUserID(String sourceUserID) {
		metadata.setSourceUserID(sourceUserID);
	}

	public String toJSON() {
		JSONObject message = new JSONObject();
		message.put("metadata", metadata.toJSON());
		message.put("content", content);
		return message.toString();
	}
	
	// generate a Message instance from JSON
	public Message() {}

	public void loadFromJSON(String message) {
		JSONObject root = new JSONObject(message);
		
		JSONObject metadataJSON = root.getJSONObject("metadata");
		String userID = metadataJSON.getString("user_id");
		metadata = new Metadata(metadataJSON);
	}

	public String getMessageType() {
		return metadata.messageType;
	}
	
	public String getMessageID() {
		return metadata.messageID;
	}
	
	public ArrayList<FileData> getFileData() {
		return metadata.data;
	}
	
	public ArrayList<String> getRequestFormats() {
		return metadata.dataRequestFormats;
	}
	
	public Map<String, ArrayList<String>> getConvertFormats() {
		return metadata.dataConvertFormats;
	}
	
	public String getOriginMessageID() {
		return metadata.originMessageID;
	}
	
	public String getSourceUserID() {
		return metadata.sourceUserID;
	}
	
	private class Metadata {
		private String userID;
		
		private String messageType;
		private String messageID;
		
		private ArrayList<FileData> 			data					= new ArrayList<FileData>();;
		
		private ArrayList<String> 				dataRequestFormats		= new ArrayList<String>();
		private Map<String, ArrayList<String>>	dataConvertFormats		= new HashMap<String, ArrayList<String>>();
		
		private String 							originMessageID;
		private String 							sourceUserID;
		
		private String							timestamp;
		
		//generate Metadata instance for sending
		private Metadata(String userID, String messageType) {
			this.userID = userID;
			this.messageType = messageType;
			this.messageID = UUID.randomUUID().toString();
			this.timestamp = Instant.now().toString();
		}
		
		private void setData(Path filepath) throws IOException {
			String filename = filepath.getFileName().toString();
			String filesize = String.valueOf(Files.size(filepath));
			data.add(new FileData(filename, filesize));
		}
		
		private void setDataRequestFormats(String...wants) {
			for (String want : wants) {
				dataRequestFormats.add(want);
			}
		}
		
		private void setDataConvertFormats(String original, String destination) {
			if (!dataConvertFormats.containsKey(original)) {
				dataConvertFormats.put(original, new ArrayList<String>());
			}
			dataConvertFormats.get(original).add(destination);
		}
		
		private void setOriginMessageID(String originMessageID) {
			this.originMessageID = originMessageID;
		}
		
		private void setSourceUserID(String sourceUserID) {
			this.sourceUserID = sourceUserID;
		}
		
		private JSONObject toJSON() {
			JSONObject meta = new JSONObject();
			meta.put("user_id", userID);
			meta.put("message_type", messageType);
			meta.put("message_id", messageID);
			meta.put("data", dataToJSON());
			meta.put("data_convert_formats", convertFormatsToJSON());
			meta.put("data_request_formats", requestFormatsToJSON());
			meta.put("origin_message_id", originMessageID);
			meta.put("source_user_id", sourceUserID);
			meta.put("timestamp", timestamp);
			return meta;
		}
		
		private JSONObject dataToJSON() {
			JSONObject data = new JSONObject();
			
			for (FileData file : this.data) {
				data.put(file.filename, file.filesize);
			}
			return data;
		}
		
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
		
		private JSONArray requestFormatsToJSON() {
			JSONArray requests = new JSONArray();
			
			for (String request : dataRequestFormats) {
				requests.put(request);
			}
			return requests;
		}
		
		// generate Metadata instance from JSON
		private Metadata(JSONObject metadataJSONObj) {
			userID = metadataJSONObj.getString("user_id");
			messageType = metadataJSONObj.getString("message_type");
			messageID = metadataJSONObj.getString("message_type");
			setData(metadataJSONObj.getJSONObject("data"));
			setDataRequestFormats(metadataJSONObj.getJSONArray("data_request_formats"));
			setDataConvertFormats(metadataJSONObj.getJSONObject("data_convert_formats"));
			originMessageID = metadataJSONObj.getString("origin_message_id");
			sourceUserID = metadataJSONObj.getString("source_user_id");
		}
		
		private void setData(JSONObject filedata) {
			Iterator<String> keys = filedata.keys();
			
			
			while (keys.hasNext()) {
				String filename = keys.next();
				String filesize = Integer.toString(filedata.getInt(filename));
				this.data.add(new FileData(filename, filesize));
			}
		}
		
		private void setDataRequestFormats(JSONArray dataRequestFormats) {
			for (Object requestFormat : dataRequestFormats) {
				this.dataRequestFormats.add(String.valueOf(requestFormat));
			}
		}
		
		private void setDataConvertFormats(JSONObject dataConvertFormats) {
			Iterator<String> keys = dataConvertFormats.keys();
			
			while (keys.hasNext()) {
				String originalFormat = keys.next();
				JSONArray destFormats = dataConvertFormats.getJSONArray(originalFormat);
				ArrayList<String> toFormats = new ArrayList<String>();
				for (Object toFormat: destFormats) {
					toFormats.add(String.valueOf(toFormat));
				}
				this.dataConvertFormats.put(originalFormat, toFormats);
			}
		}
		
		private String userID() {
			return userID;
		}
		
		public String toString() {
			return userID;
		}
	}
	
	public String toString() {
		return String.format("user_id = %s", metadata);
	}
}
