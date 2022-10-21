package constants;

import java.util.logging.Logger;

public final class Constants {
	
	// ***********************
	//
	//		Message Types
	//
	// ************************
	
	public static final String ANNOUNCE_MESSAGE 	= "announce_data";
	public static final String CAN_TRANSLATE 		= "can_translate";
	public static final String REQUEST_DATA 		= "request_data";
	
	
	// ************************
	//
	//		Metadata Keys
	//
	// ************************
	
	public static final String METADATA 			= "metadata";
	public static final String USER_ID			 	= "user_id";
	public static final String MESSAGE_ID 			= "message_id";
	public static final String MESSAGE_TYPE 		= "message_type";
	public static final String METADATA_FILEDATA 	= "data";
	public static final String DATA_CONVERT_FORMATS = "data_convert_formats";
	public static final String DATA_REQUEST_FORMATS	= "data_request_formats";
	public static final String ORIGIN_MESSAGE_ID	= "origin_message_id";
	public static final String SOURCE_USER_ID		= "source_user_id";
	public static final String TIMESTAMP			= "time_stamp";
	
	
	// ************************
	//
	//			Logger
	//
	// ************************
	
	public static final Logger LOGGER				= Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	private Constants() {}
}