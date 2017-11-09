package com.ejiopeg.rabbitmq;

import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitmqMessageSender {
	
	private final Logger logger = LoggerFactory.getLogger(RabbitmqMessageSender.class);
	
	@Value("${rabbitmq.exchange}")
	private String TEST_EXCHANGE_NAME;
	@Value("${rabbitmq.routingkey}")
	private String ROUTING_KEY;
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	public void sendMessage(final String content) {
		logger.info("exchange: "+ TEST_EXCHANGE_NAME
				+ ", routingkey: " + ROUTING_KEY);
		//String context = "Test " + new Date();
		CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
		this.rabbitTemplate.convertAndSend(TEST_EXCHANGE_NAME, ROUTING_KEY, content, correlationId);
		logger.debug("send message: " + content);
	}

}
