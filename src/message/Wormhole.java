package message;

import java.io.File;


public class Wormhole {
	
	private static String cwd = System.getProperty("user.dir");
	
	private Wormhole () {}
	
	public static void receive(String command, String filename) {
		File receivedDir = new File(cwd, "received-files");
		
		if (!receivedDir.exists()) {
			receivedDir.mkdir();
		}
		command += " --accept-file";
		
		Executive.execute(command, receivedDir);
	}
}
