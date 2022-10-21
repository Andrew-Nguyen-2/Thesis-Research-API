package research;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class User {
	private String 							userID;
	private ArrayList<String> 				want;
	private Map<String, ArrayList<String>> 	convert;
	private ArrayList<String> 				filepaths			= new ArrayList<String>();
	
	private ArrayList<String> 				messages			= new ArrayList<String>();
	private Map<String, String> 			receivedMessages 	= new HashMap<String, String>();
	
	public User() {
		userID = UUID.randomUUID().toString();
	}
}
