package message;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import rabbitmq.RabbitMQConnection;


public class Wormhole {
	
	private static String cwd = System.getProperty("user.dir");
	
	private Wormhole () {}
	
	private static String checkFilename(List<String> existingFilenames, String filename) {
		if (existingFilenames.contains(filename)) {
			while (true) {
				if (!existingFilenames.contains(filename))  {
					return filename;
				}
				String[] filenameSplit = filename.split("[.]");
				String name = filenameSplit[0];
				String format = filenameSplit[1];
				try {
					// last character is an integer
					String last = String.valueOf(name.charAt(name.length() - 1));
					int fileNum = Integer.parseInt(last);
					fileNum++;
					filename = String.format("%s-%s.%s", name.substring(0, name.length() - 2), fileNum, format);
				} catch (NumberFormatException e) {
					// last character is not an integer
					filename = String.format("%s-%s.%s", name, 2, format);
				}
			
			}
		}
		return filename;
	}
	
	/**
	 * Receive the file a user is sending.
	 * 
	 * @param command		The command to execute 'wormhole receive'.
	 * @param filename		The name of the file being received.
	 */
	public static ReceiveObj receive(String command, String filename) {
		StringBuilder commandBuilder = new StringBuilder(command);
		File receivedDir = new File(cwd, "received-files");
		
		// check if received-files directory exists, create if it does not
		if (!receivedDir.exists()) {
			receivedDir.mkdir();
		}
		
		commandBuilder.append(" --accept-file");
		
		// check if filename already exists
		List<String> existingFilenames = Arrays.asList(receivedDir.list());
		
		filename = checkFilename(existingFilenames, filename);
		commandBuilder.append(" -o " + filename);
		
		Thread running = Executive.execute(commandBuilder.toString(), receivedDir);
		return new ReceiveObj(filename, running);
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
	
	public static class ReceiveObj {
		
		private String filename;
		private Thread running;
		
		public ReceiveObj(String filename, Thread running) {
			this.filename = filename;
			this.running = running;
		}
		
		public String getFilename() {
			return this.filename;
		}
		
		public Thread getRunningThread() {
			return this.running;
		}
	}
}
