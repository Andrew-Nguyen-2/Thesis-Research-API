package research;

import java.io.IOException;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

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
		Message message = new Message(msg);
		System.out.println("message");
		System.out.println(message);
	}
	
	public void announceMessage(String message) throws IOException {
		channel.basicPublish(EXCHANGE, "announce", null, message.getBytes("UTF-8"));
	}
	
}