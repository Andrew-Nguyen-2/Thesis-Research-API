package message;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;

import constants.Constants;
import rabbitmq.RabbitMQConnection;

/**
 * Just testing exec
 * @author heddle
 *
 */
public class Executive {

	private boolean done;
	private Process process;
	

	// special command
	private static final String _cwdToFollow  = "CWD_TO_FOLLOW";
	
	// M1 Mac paths
	private static final String HOMEBREW_BIN  = "/opt/homebrew/bin";
	private static final String HOMEBREW_SBIN = "/opt/homebrew/sbin";
	private static final String HOMEBREW_PATH = ":/opt/homebrew/bin:/opt/homebrew/sbin";

	
	//current working dir
	private String _cwd;
	
	private RabbitMQConnection connection;
	private String userID;
	private String filepath;
	private String originMessageID;
	private String requestUserID;
	
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
	 * @return		true if we are running on an M1 Mac
	 */
	private boolean isM1() {
		File brewBin = new File(HOMEBREW_BIN);
		File brewSBin = new File(HOMEBREW_SBIN);
		return brewBin.exists() && brewBin.isDirectory() && brewSBin.exists() && brewSBin.isDirectory();
	}
	
	// send the message to the user requesting the data with the "wormhole receive" command
	private void sendMessage(String line) {
		Message sendData = new Message(userID, Constants.SENT_DATA);
		sendData.addFilePath(filepath);
		sendData.addOriginMessageID(originMessageID);
		sendData.addSourceUserID(userID);
		sendData.addContent(line);
		connection.direct(sendData, requestUserID);
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
								System.out.println(line);
								if (line.contains("wormhole receive")) {
									sendMessage(line);
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
								System.out.println(line);
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
								System.err.println(line);
							}
						}
						stdErrReader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					file.delete();
				} // end reader run

			};

			(new Thread(runnable)).start();
			(new Thread(reader)).start();

		} catch (Error error) {
			System.err.println(error.getMessage());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}
	
	/**
	 * Execute a command in its own process
	 * 
	 * @param command the command
	 * @param dir first cd to this directory (if not null)
	 */
	public static void execute(final String command, File dir) {
		
		Executive executive = new Executive();

		if ((dir != null) && dir.exists() && dir.isDirectory()) {
			executive.setCWD(dir.getPath());
		}

		executive.execute(command);

	}
	
	/**
	 * Execute a command in its own process
	 * 
	 * @param command the command
	 * @param dir first cd to this directory (if not null)
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
	

	//main program for testing
	public static void main(String arg[]) {
		if(isMac() ) {
			execute("say -v Karen Hello Andrew", null);	
		}
		
		//do an ls on home dir
		File  homeDir = new File( System.getProperty("user.home"));
		execute("ls -a",homeDir);
	}

}
