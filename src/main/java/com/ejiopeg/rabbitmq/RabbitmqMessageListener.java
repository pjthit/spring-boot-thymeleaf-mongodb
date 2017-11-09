package com.ejiopeg.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitmqMessageListener implements MessageListener {
	
	private final Logger logger = LoggerFactory.getLogger(RabbitmqMessageListener.class);

	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;
	
	@Override
	public void onMessage(Message message) {
		String body = new String(message.getBody());
		logger.debug("receive message: " + body);
		
		simpMessagingTemplate.convertAndSend("/topic/test", body);
		
	}

}
