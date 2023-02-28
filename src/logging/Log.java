package logging;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
	
	private static final String LOG_CLASS = Log.class.getName();
	
	public static final Logger  logger 	  = Logger.getLogger(LOG_CLASS);
	
	
	private Log() {}
	
	/**
	 * Set the output type for the logger.
	 * @param outputType  The type of logging output (file or console)
	 * @param logLevel	  The level of logging (ex. FINE, INFO, WARNING). <br>
	 * 					  Logs will be generated for levels following the one inputed. <br>
	 * 					  For example, if logLevel is set to INFO, logs will be generated for INFO and WARNING.
	 */
	public static void setOutput(String outputType, String logLevel) {
		Level level = null;
		switch (logLevel) {
			case "FINE":
				level = Level.FINE;
				break;
			case "INFO":
				level = Level.INFO;
				break;
			case "WARNING":
				level = Level.WARNING;
				break;
			default:
				level = Level.FINEST;
				break;
		}
		logger.setLevel(level);
		setOutputHelper(outputType);
	}
	
	/**
	 * Log messages not relating to message sending/ receiving.
	 * @param message	The message to be logged
	 */
	public static void other(String message) {
		logger.logp(Level.INFO, Log.class.getName(), "", message);
	}
	
	/**
	 * Log error messages.
	 * @param message		The message to be logged
	 * @param methodName	The name of the method logging the error
	 */
	public static void error(String message, String methodName) {
		logger.logp(Level.WARNING, LOG_CLASS, methodName, message);
	}
	
	/**
	 * Log error messages.
	 * @param message		The message to be logged
	 * @param className		The name of the class logging the error
	 * @param command		The command used in the Executive class
	 */
	public static void error(String message, String className, String command) {
		logger.logp(Level.WARNING, className, command, message);
	}
	
	/**
	 * Log messages for debugging.
	 * @param message		The message to be logged
	 * @param className		The name of the class logging the message
	 * @param command		The command used in the Executive class
	 */
	public static void debug(String message, String className, String command) {
		logger.logp(Level.FINE, className, command, message);
	}
	
	/**
	 * Log sent messages.
	 * @param message	Message being sent through RabbitMQ
	 */
	public static void sent(String message) {
		logger.logp(Level.INFO, LOG_CLASS, "SENT", message);
	}
	
	/**
	 * Log received messages.
	 * @param message	Message being received through RabbitMQ
	 */
	public static void received(String message) {
		logger.logp(Level.INFO, LOG_CLASS, "RECEIVED", message);
	}
	
	/**
	 * Set the handler to use a file
	 * @param outputType	The level of logging (ex. FINE, INFO, WARNING)
	 */
	private static void setOutputHelper(String outputType) {
		if (outputType.equals("file")) {
			try {
				Handler fileHandler = new FileHandler(getLogFileName());
				fileHandler.setFormatter(new SimpleFormatter());
				logger.addHandler(fileHandler);
			} catch (SecurityException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Create the directory to hold the logs if one does not already exist.
	 * @return	The name of the log file
	 */
	private static String getLogFileName() {
		String cwd = System.getProperty("user.dir");
		File logDir = new File(cwd, "output-logs");
		
		if (!logDir.exists()) {
			logDir.mkdir();
		}
		
		// file names are yyyy-MM-dd;HH:mm:ss.log
		String filename = DateTimeFormatter.ofPattern("yyyy-MM-dd;HH:mm:ss").format(LocalDateTime.now()) + ".log";
		
		return new File(logDir, filename).toString();
	}
	
//	public static void main(String[] args) {
//		String mainDirector = Log.getLogFileName();
//		System.out.println("date: " + mainDirector);
//	}

}
