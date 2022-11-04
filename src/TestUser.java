import api.ResearchAPI;

public class TestUser {
	
	public static void main(String[] args) {
		ResearchAPI user = new ResearchAPI();
		
		user.connect("anguyen");
		user.addConvertFormat("csv", "json");
//		user.addWantFormats("csv");
//		user.addFile("/Users/andrewnguyen/eclipse-workspace/research/test-files/test-data.csv");
		user.startListening();
		while (true) {
			String receivedFilepath = user.getReceivedFilepath();
			if (receivedFilepath != null) {
				System.out.println("\nreceivedFilepath: " + receivedFilepath + "\n");
				// translate file data
			}
		}
	}
}
