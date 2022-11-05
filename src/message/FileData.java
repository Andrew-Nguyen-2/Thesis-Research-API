package message;


/**
 * Object to hold the file data.
 * @author andrewnguyen
 *
 */
public class FileData {
	
	private String filename;
	private String filesize;
	
	/**
	 * Constructor
	 * 
	 * @param filename		The name of the file.
	 * @param filesize		The size of the file.
	 */
	public FileData(String filename, String filesize) {
		this.filename = filename;
		this.filesize = filesize;
	}
	
	/**
	 * Get the name of the file.
	 * 
	 * @return
	 */
	public String getFileName() {
		return this.filename;
	}
	
	/**
	 * Get the size of the file.
	 * 
	 * @return
	 */
	public String getFileSize() {
		return this.filesize;
	}
	
	
	/**
	 * To print an instance of a file.
	 */
	public String toString() {
		return String.format("(%s, %s)", filename, filesize);
	}

}
