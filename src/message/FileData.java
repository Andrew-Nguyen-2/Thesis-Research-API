package message;


public class FileData {
	
	private String filename;
	private String filesize;
	
	public FileData(String filename, String filesize) {
		this.filename = filename;
		this.filesize = filesize;
	}
	
	public String getFileName() {
		return this.filename;
	}
	
	public String getFileSize() {
		return this.filesize;
	}
	
	
	public String toString() {
		return String.format("(%s, %s)", filename, filesize);
	}

}
