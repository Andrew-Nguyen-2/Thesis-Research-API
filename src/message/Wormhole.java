package message;


import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import rabbitmq.RabbitMQConnection;

/**
 * Send and receive files using magic-wormhole.
 * @author andrewnguyen
 *
 */
public class Wormhole {
	
	private static String cwd = System.getProperty("user.dir");
	
	private Wormhole () {}
	
	/**
	 * Changes filename if it already exists.
	 * 
	 * @param existingFilenames		The list of existing filenames in the directory.
	 * @param filename				The filename received.
	 * @return						The filename unchanged if it does not exist or the filename with a number at the end starting at 2.
	 */
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
	 * @param command		The <i>"wormhole receive"</i> command.
	 * @param filename		The name of the file being received.
	 * @return 				A ReceivedObj object containing the name of the file received and the running <i>wormhole receive</i> thread.
	 */
	public static ReceiveObj receive(String command, String filename, String senderID) {
		StringBuilder commandBuilder = new StringBuilder(command);
		File receivedDir = new File(cwd, "received-files");
		String originalFilename = filename;
		
		// check if received-files directory exists, create if it does not
		if (!receivedDir.exists()) {
			receivedDir.mkdir();
		}
		
		// to bypass "yes" input for wormhole receive
		commandBuilder.append(" --accept-file");
		
		// check if filename already exists
		List<String> existingFilenames = Arrays.asList(receivedDir.list());
		filename = checkFilename(existingFilenames, filename);
		
		// receive the file as the filename
		commandBuilder.append(" -o " + filename);
		
		Thread running = Executive.execute(commandBuilder.toString(), receivedDir);
		return new ReceiveObj(filename, running, senderID, originalFilename);
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
		
		private String newFilename;
		private Thread running;
		
		private String sourceUserID;
		private String originalFilename;
		
		public ReceiveObj(String newFilename, Thread running, String sourceUserID, String originalFilename) {
			this.newFilename = newFilename;
			this.running = running;
			this.sourceUserID = sourceUserID;
			this.originalFilename = originalFilename;
		}
		
		public String getNewFilename() {
			return this.newFilename;
		}
		
		public Thread getRunningThread() {
			return this.running;
		}
		
		public String getSourceUserID() {
			return this.sourceUserID;
		}
		
		public String getOriginalFilename() {
			return this.originalFilename;
		}
	}
}
