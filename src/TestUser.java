import api.ResearchAPI;

public class TestUser {
	
	public static void main(String[] args) {
		ResearchAPI user = new ResearchAPI();
		
		user.connect("anguyen");
		user.addConvertFormat("csv", "json");
//		user.addWantFormats("csv");
//		user.addFile("/Users/andrewnguyen/eclipse-workspace/research/test-files/test-data.csv");
		while (true) {
			user.getNextMessage();
			System.out.println("\nreceivedFilepath: " + user.getReceivedFilepath() + "\n");
		}
	}
}
