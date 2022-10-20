package research;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import org.json.JSONArray;


public class ResearchAPI {
	
	public String userID;
	private ArrayList<String> want = new ArrayList<String>();
	private Map<String, ArrayList<String>> convert = new HashMap<String, ArrayList<String>>();
	
	private boolean messagesAvailable = false;
	
	public ResearchAPI() {
		userID = UUID.randomUUID().toString();
	}
	
	public void addWantFormats(String ... wantFormats) {
		for (String format : wantFormats) {
			want.add(format);
		}
	}
	
	public void addFile(String filepath) {
		
	}
	
	public void addConvertFormat(String originalFormat, String destinationFormat) {
		if (!convert.containsKey(originalFormat)) {
			convert.put(originalFormat, new ArrayList<String>());
		}
		
		convert.get(originalFormat).add(destinationFormat);
	}
	
	public void connect() throws IOException, TimeoutException {
		RabbitMQClient rmqClient = new RabbitMQClient(this);
	}
	
	
	public static void main(String[] args) throws Exception {
		ResearchAPI user = new ResearchAPI();
		user.connect();
	}
	
}

