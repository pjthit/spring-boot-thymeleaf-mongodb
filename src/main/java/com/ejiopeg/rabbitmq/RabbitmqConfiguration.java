package com.ejiopeg.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.rabbitmq.client.Channel;

@Configuration
public class RabbitmqConfiguration {
	
	private final Logger logger = LoggerFactory.getLogger(RabbitmqConfiguration.class);
	
	@Value("${rabbitmq.queuename}")
	private String TEST_QUEUE_NAME;
	@Value("${rabbitmq.exchange}")
	private String TEST_EXCHANGE_NAME;
	@Value("${rabbitmq.routingkey}")
	private String ROUTING_KEY;
	
	@Value("${rabbitmq.host}")
    private String rabbitmqHost;
    @Value("${rabbitmq.port}")
    private Integer rabbitmqPort;
    @Value("${rabbitmq.username}")
    private String rabbitmqUsername;
    @Value("${rabbitmq.password}")
    private String rabbitmqPassword;
    @Value("${rabbitmq.virtualHost}")
    private String rabbitmqVirtualHost;
    
    @Bean
    public ConnectionFactory connectionFactory() {
    	CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitmqHost, rabbitmqPort);
    	connectionFactory.setUsername(rabbitmqUsername);
    	connectionFactory.setPassword(rabbitmqPassword);
    	connectionFactory.setVirtualHost(rabbitmqVirtualHost);
    	return connectionFactory;
    }
    
    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate() {
    	RabbitTemplate template = new RabbitTemplate(connectionFactory());
    	//template.setMessageConverter(jackson2JsonMessageConverter());
    	template.setExchange(TEST_EXCHANGE_NAME);
    	return template;
    }
    
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(TEST_EXCHANGE_NAME);
    }
    
    @Bean  
    public Queue queue() {  
        return new Queue(TEST_QUEUE_NAME, true);
  
    }  
    
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(exchange()).with(ROUTING_KEY);
    }

    @Bean
    public RabbitmqMessageListener receiver() {
    	return new RabbitmqMessageListener();
    }
    
    @Bean  
    public SimpleMessageListenerContainer messageContainer() {  
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(); 
        container.setConnectionFactory(connectionFactory());
        container.setQueues(queue());
        container.setMessageListener(receiver());
        /*container.setMessageListener(new ChannelAwareMessageListener() {

			@Override
			public void onMessage(Message message, Channel channel) throws Exception {
				// TODO Auto-generated method stub
				System.out.println("receive: " + new String(message.getBody()));
			}
        	
        });*/
        return container;
    }
}
