package research;

import java.io.IOException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;

public class RabbitMQClient {
	
	private ResearchAPI user;
	private static final String EXCHANGE = "milestone1";
	private Queue<String> messageQueue = new LinkedList<>();
	private Channel channel;
	
	public RabbitMQClient(ResearchAPI user) throws IOException, TimeoutException {
		this.user = user;
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		channel = connection.createChannel();
		
		channel.exchangeDeclare(EXCHANGE, "topic");
		String queueName = channel.queueDeclare().getQueue();
		
		channel.queueBind(queueName, EXCHANGE, "announce");
		channel.queueBind(queueName, EXCHANGE, this.user.userID);
		
		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
		
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			String message = new String(delivery.getBody(), "UTF-8");
			messageQueue.add(message);
			System.out.println(" [x] Received '" + message + "'");
			processMessage(message);
		};
		
		channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
	}
	
	public void processMessage(String msg) throws JsonParseException, JsonMappingException, IOException {
		Map<String, String> message = new HashMap<String, String>();
		JSONObject root = new JSONObject(msg);
		
		JSONObject metadataJSON = root.getJSONObject("metadata");
		String userID = metadataJSON.getString("user_id");
		String messageType = metadataJSON.getString("message_type");
		String messageID = metadataJSON.getString("message_id");
		JSONObject filedata = metadataJSON.getJSONObject("data");
		JSONObject dataConvertFormats = metadataJSON.getJSONObject("data_convert_formats");
		JSONArray dataRequestFormats = metadataJSON.getJSONArray("data_request_formats");
		String originMessageID = metadataJSON.getString("origin_message_id");
		String sourceUserID = metadataJSON.getString("source_user_id");
		
		Metadata metadata = new Metadata(userID, messageType, messageID);
		metadata.setData(filedata);
		metadata.setDataConvertFormats(dataConvertFormats);
		metadata.setDataRequestFormats(dataRequestFormats);
		metadata.setOriginMessageID(originMessageID);
		metadata.setSourceUserID(sourceUserID);
		
		System.out.println("metadata");
		System.out.println(metadata);
		
	}
	
	public void announceMessage(String message) throws IOException {
		channel.basicPublish(EXCHANGE, "announce", null, message.getBytes("UTF-8"));
	}
	
}
