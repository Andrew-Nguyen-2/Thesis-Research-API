package message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import constants.Constants;

public class Metadata {
    public String userID;

    public String messageType;
    public String messageID;

    public ArrayList<FileData> data = new ArrayList<>();

    public ArrayList<String> dataRequestFormats = new ArrayList<>();
    public Map<String, ArrayList<String>> dataConvertFormats = new HashMap<>();

    public String originMessageID = "";
    public String sourceUserID = "";

    public String timestamp;

    // ****************************************
    //
    // generate Metadata instance for sending
    //
    // ****************************************

    /**
     * Constructor for Metadata when sending.
     * 
     * @param userID      The ID of the user sending the message.
     * @param messageType The message type for the message.
     */
    public Metadata(String userID, String messageType) {
        this.userID = userID;
        this.messageType = messageType;
        this.messageID = UUID.randomUUID().toString();
        this.timestamp = Instant.now().toString();
    }

    /**
     * Add the file path to the array list of FileData instances.
     * 
     * @param filepath The file path of the file shared.
     * @throws IOException
     */
    public void setData(Path filepath) throws IOException {
        String filename = filepath.getFileName().toString();
        String filesize = String.valueOf(Files.size(filepath));
        data.add(new FileData(filename, filesize));
    }

    /**
     * Add the requested file data to the metadata.
     * 
     * @param filedata The file data the user is requesting.
     */
    public void setData(FileData filedata) {
        data.add(filedata);
    }

    /**
     * Add the request formats.
     * 
     * @param wants The formats the user wants
     */
    public void setDataRequestFormats(String... wants) {
        this.dataRequestFormats.addAll(Arrays.asList(wants));
    }

    /**
     * Add the original format to the map and destination format to the respected
     * list.
     * 
     * @param original    The original format.
     * @param destination The destination format.
     */
    public void setDataConvertFormats(String original, String destination) {
        dataConvertFormats.putIfAbsent(original, new ArrayList<>());
        dataConvertFormats.get(original).add(destination);
    }

    /**
     * Set the origin message ID.
     * 
     * @param originMessageID
     */
    public void setOriginMessageID(String originMessageID) {
        this.originMessageID = originMessageID;
    }

    /**
     * Set the source user ID.
     * 
     * @param sourceUserID
     */
    public void setSourceUserID(String sourceUserID) {
        this.sourceUserID = sourceUserID;
    }

    /**
     * Convert the Metadata instance to a JSONObject.
     * 
     * @return JSONObject.
     */
    public JSONObject toJSON() {
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
     * @return JSONArray.
     */
    public JSONArray dataToJSON() {
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
     * Convert the map of original formats and their respected array list of
     * destination formats to a JSONObject.
     * 
     * @return JSONArray.
     */
    public JSONArray convertFormatsToJSON() {
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
     * @return JSONArray.
     */
    public JSONArray requestFormatsToJSON() {
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
     * @param metadataJSONObj The metadata as a JSONObject.
     */
    public Metadata(JSONObject metadataJSONObj) {
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
     * @param filedata The JSONArray containing the filename and file size.
     */
    public void setData(JSONArray filedata) {
        for (Object file : filedata) {
            String filename = ((JSONObject) file).getString(Constants.FILENAME);
            String filesize = Integer.toString(((JSONObject) file).getInt(Constants.FILESIZE));
            this.data.add(new FileData(filename, filesize));
        }

    }

    /**
     * Set the data request formats after parsing JSONArray containing the formats.
     * 
     * @param dataRequestFormats The JSONArray containing request formats.
     */
    public void setDataRequestFormats(JSONArray dataRequestFormats) {
        for (Object requestFormat : dataRequestFormats) {
            this.dataRequestFormats.add(String.valueOf(requestFormat));
        }
    }

    /**
     * Set the data convert formats after parsing the JSONArray containing the
     * formats.
     * 
     * @param dataConvertFormats The JSONArray containing the convert formats.
     */
    public void setDataConvertFormats(JSONArray dataConvertFormats) {
        for (Object convert : dataConvertFormats) {
            ArrayList<String> destFormats = new ArrayList<>();
            for (Object toFormat : ((JSONObject) convert).getJSONArray(Constants.DESTINATION_FORMATS)) {
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
        String metadataString = String.format("user_id: %s, message_id: %s, message_type: %s%n", userID, messageID,
                messageType);
        metadataString += String.format("data: %s, data_convert_formats: %s, data_request_formats: %s%n", data,
                dataConvertFormats, dataRequestFormats);
        metadataString += String.format("timestamp: %s, origin_message_id: %s, source_user_id: %s%n", timestamp,
                originMessageID, sourceUserID);

        return metadataString;
    }
}
