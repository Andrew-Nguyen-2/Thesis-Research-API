package rabbitmq;

import java.util.LinkedList;
import java.util.Queue;

import user.User;

public class MessageThread extends Thread {
	
	private String userID;
	private Queue<String> messageQueue;
	
	public MessageThread(User user) {
		this.userID = user.getUserID();
		messageQueue = new LinkedList<>();
	}
}
