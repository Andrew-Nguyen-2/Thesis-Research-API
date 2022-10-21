package rabbitmq;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import constants.Constants;
import user.User;

public class MessageThread extends Thread {
	
	private String userID;
	private RabbitMQConnection connection;
	private Channel channel;
	private String queueName;
	private Queue<String> messageQueue;
	private Lock lock;
	
	public MessageThread(User user, RabbitMQConnection rabbitConnection, Lock lock) {
		this.userID = user.getUserID();
		this.connection = rabbitConnection;
		this.channel = rabbitConnection.getChannel();
		this.queueName = rabbitConnection.getQueueName();
		
		messageQueue = new LinkedList<>();
		this.lock = lock;
	}
	
	@Override
	public void run() {
		try {
			lock.lock();
			DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
				messageQueue.add(message);
				notify();
			};
			
			channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
}
