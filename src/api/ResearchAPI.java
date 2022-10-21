package api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import user.User;


public class ResearchAPI {
	
	public User 								user;
	private ArrayList<String> 					want;
	private Map<String, ArrayList<String>>		convert;
	
	private boolean messagesAvailable = false;
	
	public ResearchAPI() {
		user = new User();
		want = new ArrayList<String>();
		convert = new HashMap<String, ArrayList<String>>();
	}
	
	public void addWantFormats(String ... wantFormats) {
		for (String format : wantFormats) {
			want.add(format);
		}
	}
	
	public void addFile(String filepath) {
		try {
			Path validPath = Paths.get(filepath);
			long bytes = Files.size(validPath);
			
		} catch (InvalidPathException | IOException e) {
			System.out.println(e);
		}
	}
	
	public void addConvertFormat(String originalFormat, String destinationFormat) {
		if (!convert.containsKey(originalFormat)) {
			convert.put(originalFormat, new ArrayList<String>());
		}
		
		convert.get(originalFormat).add(destinationFormat);
	}
	
	public void connect() {
		
	}
	
	public void getNextMessage() {
		
	}
	
}

