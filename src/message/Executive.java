package message;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;

import constants.Constants;
import logging.Log;
import rabbitmq.RabbitMQConnection;

/**
 * Just testing exec
 * @author heddle
 *
 */
public class Executive {

	private boolean 			done;
	private Process 			process;
	private Thread				running;

	// special command
	private static final String _cwdToFollow  = "CWD_TO_FOLLOW";
	
	// M1 Mac paths
	private static final String HOMEBREW_BIN  = "/opt/homebrew/bin";
	private static final String HOMEBREW_SBIN = "/opt/homebrew/sbin";
	private static final String HOMEBREW_PATH = ":/opt/homebrew/bin:/opt/homebrew/sbin";
	
	private static final String CLASS_NAME 	  = Executive.class.getName();

	// current working dir
	private String 				_cwd;
	
	// for executing wormhole send
	private RabbitMQConnection 	connection;
	private String 				userID;
	private String 				filepath;
	private String 				originMessageID;
	private String 				requestUserID;
	
	
	// message sent to request the data
	private Message 			requestMessage;
	
	public Executive() {
		done = false;
	}
	
	/**
	 * Set the cwd
	 * @param cwd new working dir
	 */
	public void setCWD(String cwd) {
		_cwd = cwd;
	}
	
	/**
	 * Set the user's RabbitMQ Connection.
	 * @param connection		The connection corresponding to the user.
	 */
	public void setConnection(RabbitMQConnection connection) {
		this.connection = connection;
	}
	
	/**
	 * Set the ID the of the user.
	 * @param userID
	 */
	public void setUserID(String userID) {
		this.userID = userID;
	}
	
	/**
	 * Set the path where the file is located.
	 * @param filepath
	 */
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	
	/**
	 * Set the message that the user received.
	 * @param message
	 */
	public void setRequiredMessageContent(Message message) {
		this.originMessageID = message.getOriginMessageID();
		this.requestUserID = message.getSenderID();
	}
	
	public void setRequestMessage(Message message) {
		this.requestMessage = message;
	}
	
	/**
	 * Check whether we are running on a Mac
	 * Only used for the "talking" test
	 * @return <code>true</code> if we are running on a Mac
	 */
	public static boolean isMac() {
		String osName = System.getProperty("os.name");
		return osName.toLowerCase().startsWith("mac");
	}
	
	/**
	 * Check if we are running on an M1 Mac
	 * @return	<code>true</code> if we are running on an M1 Mac
	 */
	private boolean isM1() {
		File brewBin = new File(HOMEBREW_BIN);
		File brewSBin = new File(HOMEBREW_SBIN);
		return brewBin.exists() && brewBin.isDirectory() && brewSBin.exists() && brewSBin.isDirectory();
	}
	
	/**
	 * Send the message to the user requesting the data containing the <i>"wormhole receive"</i> command
	 * @param command		the command the other user will use to receive the data
	 */
	private void sendMessage(String command) {
		Message sendData = new Message(userID, Constants.SENT_DATA);
		sendData.addFilePath(filepath);
		sendData.addOriginMessageID(originMessageID);
		sendData.addSourceUserID(userID);
		sendData.addContent(command);
		connection.direct(sendData, requestUserID);
	}
	
	
	private void requestDataAgain() {
		String originSenderID = this.requestMessage.getSourceUserID();
		connection.direct(requestMessage, originSenderID);
	}
	

	// build a script file around the command
	//the command is execute in its own process
	private File tempFileScript(String command) {

		File file = null;
		try {
			file = File.createTempFile("bCNU", null);

			PrintWriter printWriter = new PrintWriter(file);
			printWriter.write("#!/bin/bash\n");

			//change working dir?
			if (_cwd != null) {
				printWriter.write("echo " + _cwdToFollow + "\n");
				printWriter.write("cd " + _cwd + "\n");
				printWriter.write("pwd" + "\n");
			}
			
			// update environment path if running on M1 Mac
			if (isMac() && isM1()) {
				String path = System.getenv("PATH") + HOMEBREW_PATH;
				printWriter.write("export PATH=" + path + "\n");
			}

			printWriter.write(command + "\n");
			printWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return file;
	}

	//execute the command contained in the process record
	//capture the output
	private void execute(String command) {

		final File file = tempFileScript(command);
		if (file == null) {
			return;
		}

		try {
			ProcessBuilder pb = new ProcessBuilder();
			pb.command("bash", file.getPath());
			pb.redirectErrorStream(true);
			process = pb.start();
			if (process == null) {
				return;
			}
			
			final BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			final BufferedReader stdErrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			// this will just block until process ends
			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					try {
						process.waitFor();
						done = true;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}

			};


			//get the process output and print to console
			Runnable reader = new Runnable() {

				@Override
				public void run() {
					try {
						while (!done) {
							String line = stdOutReader.readLine();
							if (line != null) {
								Log.debug(line, CLASS_NAME, command);
								if (line.contains("wormhole receive")) {
									sendMessage(line);
								}
								if (line.contains("ERROR") && command.contains("receive")) {
									// check if user is attempting to receive the message and it is magic-wormhole failed
									requestDataAgain();
								}
								
							} else {
								try {
									Thread.sleep(50);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
						// flush final lines after process ended
						process.getOutputStream().flush();
						boolean reading = true;
						while (reading) {
							String line = stdOutReader.readLine();
							if (line == null) {
								reading = false;
							} else {
								Log.debug(line, CLASS_NAME, command);
							}
						}
						// really done
						stdOutReader.close();

						reading = true;
						while (reading) {
							String line = stdErrReader.readLine();
							if (line == null) {
								reading = false;
							} else {
								Log.error(line, CLASS_NAME, command);
							}
						}
						stdErrReader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					file.delete();
				} // end reader run

			};

			running = new Thread(runnable);
			running.start();
			(new Thread(reader)).start();

		} catch (Error | Exception error) {
			Log.error(error.getMessage(), CLASS_NAME, command);
		}

	}
	
	/**
	 * Get the thread waiting for the process to finish
	 * @return		the running thread
	 */
	private Thread getRunningThread() {
		return this.running;
	}
	
	/**
	 * Execute a command in its own process, used in checkKnownHosts method in RabbitMQConnection file
	 * 
	 * @param command	the command
	 * @param dir 		first cd to this directory (if not null)
	 * @return 			the running thread
	 */
	public static Thread execute(final String command, File dir) {
		
		Executive executive = new Executive();

		if ((dir != null) && dir.exists() && dir.isDirectory()) {
			executive.setCWD(dir.getPath());
		}

		executive.execute(command);
		return executive.getRunningThread();
	}
	
	/**
	 * Execute a command in its own process, used in receive method in Wormhole file
	 * 
	 * @param command	the command
	 * @param dir 		first cd to this directory (if not null)
	 * @return 			the running thread
	 */
	public static Thread execute(final String command, File dir, RabbitMQConnection connection, Message requestMessage) {
		
		Executive executive = new Executive();

		if ((dir != null) && dir.exists() && dir.isDirectory()) {
			executive.setCWD(dir.getPath());
		}
		
		executive.setRequestMessage(requestMessage);
		executive.setConnection(connection);
		executive.execute(command);
		return executive.getRunningThread();
	}
	
	
	/**
	 * Execute a command in its own process, used in send method in Wormhole file
	 * 
	 * @param command 		the command
	 * @param dir 			first cd to this directory (if not null)
	 * @param connection	the rabbitmq connection for this user
	 * @param userID		the ID of the user
	 * @param message		the message requesting the data
	 * @param filepath		the file path where the data is held
	 */
	public static void execute(final String command, File dir, RabbitMQConnection connection, String userID, Message message, Path filepath) {
		
		Executive executive = new Executive();
		if ((dir != null) && dir.exists() && dir.isDirectory()) {
			executive.setCWD(dir.getPath());
		}
		
		executive.setRequiredMessageContent(message);
		executive.setFilepath(filepath.toString());
		executive.setUserID(userID);
		executive.setConnection(connection);
		executive.execute(command);

	}
}
