import api.ResearchAPI;

public class TestUser {
	
	public static void main(String[] args) {
		ResearchAPI user = new ResearchAPI();
		
		user.addConvertFormat("csv", "json");
		user.connect();
//		user.addFile("/Users/andrewnguyen/eclipse-workspace/research/test-files/test-data.csv");
		user.getNextMessage();
	}
}
