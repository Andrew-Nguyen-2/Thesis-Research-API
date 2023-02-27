package logging;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
	
	private static final Logger logger = Logger.getLogger(Log.class.getName());
	
	private Log() {
		logger.setLevel(Level.INFO);
	}
	
	/**
	 * Set the output type for the logger.
	 * @param outputType  The type of logging output (file or console)
	 */
	public static void setOutput(String outputType) {
		if (outputType.equals("file")) {
			try {
				Handler fileHandler = new FileHandler(getLogFileName());
				fileHandler.setFormatter(new SimpleFormatter());
				logger.addHandler(fileHandler);
			} catch (SecurityException | IOException e) {
				e.printStackTrace();
			}
		} else {
			logger.addHandler(new ConsoleHandler());
		}
	}
	
	/**
	 * Create the directory to hold the logs if one does not already exist.
	 * @return	The log directory
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
