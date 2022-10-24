package message;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import constants.Constants;
import user.User;

public class ProcessMessage {
	
	private ProcessMessage() {}
	
	public static void process(User user, String receivedMessage) {
		Message message = new Message(receivedMessage);
		String senderID = message.getSenderID();
		String messageType = message.getMessageType();
		
		String userID = user.getUserID();
		List<Path> filepaths = user.getFilepaths();
		List<String> wantFormats = user.getWantFormats();
		Map<String, ArrayList<String>> convertFormats = user.getConvertFormats();
		
		if (!Objects.equals(senderID, userID)) {
			System.out.println(String.format(" [x] Received %s", message));
			user.addReceivedMessage(message.getMessageID(), message);
			
			if (!wantFormats.isEmpty()) {
				switch (messageType) {
				
				case Constants.ANNOUNCE_MESSAGE:
					wantData(user, message);
					break;
				case Constants.REQUEST_DATA:
					break;
				default: break;
				
				}
			}
		}
	}
	
	private static void wantData(User user, Message receivedMessage) {
		// 
	}

}
