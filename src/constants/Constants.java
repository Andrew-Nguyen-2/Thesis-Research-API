package constants;

public final class Constants {

	// ***********************
	//
	// Message Types
	//
	// ************************

	public static final String ANNOUNCE_MESSAGE = "announce_data";
	public static final String CAN_TRANSLATE = "can_translate";
	public static final String REQUEST_DATA = "request_data";
	public static final String SENT_DATA = "sent_data";

	// ************************
	//
	// Metadata Keys
	//
	// ************************

	public static final String METADATA = "metadata";
	public static final String USER_ID = "user_id";
	public static final String MESSAGE_ID = "message_id";
	public static final String MESSAGE_TYPE = "message_type";
	public static final String METADATA_FILEDATA = "data";
	public static final String DATA_CONVERT_FORMATS = "data_convert_formats";
	public static final String DATA_REQUEST_FORMATS = "data_request_formats";
	public static final String ORIGIN_MESSAGE_ID = "origin_message_id";
	public static final String SOURCE_USER_ID = "source_user_id";
	public static final String TIMESTAMP = "time_stamp";
	public static final String ORIGINAL_FORMAT = "original_format";
	public static final String DESTINATION_FORMATS = "destination_formats";
	public static final String FILENAME = "filename";
	public static final String FILESIZE = "filesize";

	public static final String CONTENT = "content";

	// **********************************
	//
	// RabbitMQ Guest User Information
	//
	// **********************************

	// TODO: find more secure way of storing variables for distribution
	public static final String RABBITMQ_URI = "amqps://b-ea204bc2-fa85-44c6-aa3d-26418a344982.mq.us-east-2.amazonaws.com:5671";
	public static final String USERNAME = "guest";
	public static final String PASSWORD = "tyfdox-3hAmba-fosgyr";

	private Constants() {
	}
}
