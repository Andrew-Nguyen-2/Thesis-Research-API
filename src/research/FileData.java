package research;

public class FileData {
	
	public String filename;
	public String filesize;
	
	public FileData(String filename, String filesize) {
		this.filename = filename;
		this.filesize = filesize;
	}
	
	
	public String toString() {
		return String.format("(%s, %s)", filename, filesize);
	}

}
