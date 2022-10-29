package message;

import java.io.File;
import java.util.Arrays;
import java.util.List;


public class Wormhole {
	
	private static String cwd = System.getProperty("user.dir");
	
	private Wormhole () {}
	
	public static void receive(String command, String filename) {
		StringBuilder commandBuilder = new StringBuilder(command);
		File receivedDir = new File(cwd, "received-files");
		
		// check if received-files directory exists, create if it does not
		if (!receivedDir.exists()) {
			receivedDir.mkdir();
		}
		
		commandBuilder.append(" --accept-file");
		
		// check if filename already exists
		List<String> existingFilenames = Arrays.asList(receivedDir.list());
		int count = 2;
		
		while (true) {
			if (!existingFilenames.contains(filename)) {
				break;
			}
			String[] filenameSplit = filename.split("[.]");
			filename = String.format("%s-%s.%s", filenameSplit[0], count, filenameSplit[1]);
			commandBuilder.append(" -o " + filename);
			count++;
		}
		
		Executive.execute(commandBuilder.toString(), receivedDir);
	}
}
