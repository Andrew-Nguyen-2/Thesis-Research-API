import api.ResearchAPI;

public class TestUser {

	public static void main(String[] args) {
		ResearchAPI api = new ResearchAPI("console", "FINEST");
		api.connect("andrew-nguyen", "");
		api.addConvertFormat("csv", "png");
		api.addWantFormats("csv");
		api.startListening();
		while (true) {
			String[] receivedFile = api.getReceivedFile();
			String receivedFilepath = receivedFile[0];
			String receivedFileFormat = receivedFile[1];
			if (receivedFilepath != null && receivedFileFormat != null && receivedFileFormat.equals("csv")) {
				// do translation here
			}
		}
	}
}
