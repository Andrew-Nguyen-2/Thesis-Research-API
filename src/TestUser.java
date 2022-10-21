

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import api.ResearchAPI;

public class TestUser {
	
	
	public static void main(String[] args) throws IOException, TimeoutException {
		ResearchAPI user = new ResearchAPI();
		
		user.addWantFormats("csv", "json");
		user.addConvertFormat("csv", "scatter");
		user.connect();
	}

}
