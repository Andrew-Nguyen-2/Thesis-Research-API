package api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import rabbitmq.RabbitMQConnection;
import user.User;


public class ResearchAPI {
	
	public User 								user;
	private RabbitMQConnection 					connection;
	private ArrayList<String> 					want;
	private Map<String, ArrayList<String>>		convert;
	
	public ResearchAPI() {
		user = new User();
		want = new ArrayList<>();
		convert = new HashMap<>();
	}
	
	public void addWantFormats(String ... wantFormats) {
		want.addAll(Arrays.asList(wantFormats));
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
		convert.putIfAbsent(originalFormat, new ArrayList<>());
		convert.get(originalFormat).add(destinationFormat);
	}
	
	public void connect() {
		try {
			this.connection = new RabbitMQConnection(user);
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
	}
	
	public void getNextMessage() {
		
	}
	
}

