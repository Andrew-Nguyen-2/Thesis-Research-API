package message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Wormhole {
	
	private static String userDirectory = System.getProperty("user.dir");
	
	private Wormhole () {}
	
	public static void receive(String command) throws IOException {
		Path newDirPath = Paths.get(userDirectory, "received-files");
		File newDirFile = new File(newDirPath.toString());
		
//		if (!newDirFile.exists()) {
//			newDirFile.mkdir();
//		}
//		
//		Process process = Runtime.getRuntime().exec(command);
//		System.out.println("Current directory 1 " + userDirectory);
//		String[] newCommand = new String[] {"cd", "received-files"};
//		Process process = new ProcessBuilder(newCommand).start();
//		System.out.println("Current directory: " + System.getProperty("user.dir"));
	}
}
