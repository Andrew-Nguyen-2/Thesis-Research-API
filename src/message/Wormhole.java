package message;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import rabbitmq.RabbitMQConnection;


public class Wormhole {
	
	private static String cwd = System.getProperty("user.dir");
	
	private Wormhole () {}
	
	/**
	 * Receive the file a user is sending.
	 * 
	 * @param command		The command to execute 'wormhole receive'.
	 * @param filename		The name of the file being received.
	 */
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
	
	/**
	 * Send the file the user is requesting.
	 * 
	 * @param connection		The RabbitMQ connection corresponding to the user.
	 * @param userID			The ID of the user sending the data.
	 * @param message			The message the user received requesting the data.
	 * @param filepath			The path of the file the user is requesting.
	 */
	public static void send(RabbitMQConnection connection, String userID, Message message, Path filepath) {
		Executive.execute("wormhole send " + filepath, new File(cwd), connection, userID, message, filepath);
	}
}
