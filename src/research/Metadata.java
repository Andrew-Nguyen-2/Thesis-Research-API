package research;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class Metadata {
	
	public String 							userID;
	
	public String 							messageType;
	public String 							messageID;
	
	public ArrayList<FileData> 				data					= new ArrayList<FileData>();;
	
	public ArrayList<String> 				dataRequestFormats		= new ArrayList<String>();
	public Map<String, ArrayList<String>>	dataConvertFormats		= new HashMap<String, ArrayList<String>>();
	
	public String 							originMessageID;
	public String 							sourceUserID;
	
	
	
	public Metadata(String userID, String messageType, String messageID) {
		this.userID = userID;
		this.messageType = messageType;
		this.messageID = messageID;
	}
	
	
	public void setData(JSONObject data) {
		Iterator<String> keys = data.keys();
		
		
		while (keys.hasNext()) {
			String filename = keys.next();
			String filesize = Integer.toString(data.getInt(filename));
			this.data.add(new FileData(filename, filesize));
		}
	}
	
	public void setDataRequestFormats(JSONArray dataRequestFormats) {
		for (Object requestFormat : dataRequestFormats) {
			this.dataRequestFormats.add(String.valueOf(requestFormat));
		}
	}
	
	public void setDataConvertFormats(JSONObject dataConvertFormats) {
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
	
	public ArrayList<String> getDestinationFormats(String originFormat) {
		return dataConvertFormats.get(originFormat);
	}
	
	public void setOriginMessageID(String originMessageID) {
		this.originMessageID = originMessageID;
	}
	
	public void setSourceUserID(String sourceUserID) {
		this.sourceUserID = sourceUserID;
	}
	
	public String toString() {
		String out = String.format("user_id = %s, message_id = %s, message_type = %s\n", userID, messageID, messageType);
		out += String.format("data = %s, data_request_formats = %s, data_convert_formats = %s\n", data, dataRequestFormats, dataConvertFormats);
		out += String.format("origin_message_id = %s, source_user_id = %s\n", originMessageID, sourceUserID);
		return out;
	}

}
