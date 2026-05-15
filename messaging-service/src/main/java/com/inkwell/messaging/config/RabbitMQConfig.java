package com.inkwell.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for messaging queues and exchanges.
 */
@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String NEWSLETTER_QUEUE = "newsletter_queue";
    public static final String NOTIFICATION_QUEUE = "notification_queue";

    // Exchange names
    public static final String NEWSLETTER_EXCHANGE = "newsletter_exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification_exchange";

    // Routing keys
    public static final String NEWSLETTER_ROUTING_KEY = "newsletter.new_post";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.created";

    /**
     * Newsletter queue for new post notifications.
     */
    @Bean
    public Queue newsletterQueue() {
        return new Queue(NEWSLETTER_QUEUE, true);
    }

    /**
     * Notification queue for user notifications.
     */
    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    /**
     * Newsletter exchange.
     */
    @Bean
    public TopicExchange newsletterExchange() {
        return new TopicExchange(NEWSLETTER_EXCHANGE);
    }

    /**
     * Notification exchange.
     */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    /**
     * Binding for newsletter queue.
     */
    @Bean
    public Binding newsletterBinding(Queue newsletterQueue, TopicExchange newsletterExchange) {
        return BindingBuilder.bind(newsletterQueue).to(newsletterExchange).with(NEWSLETTER_ROUTING_KEY);
    }

    /**
     * Binding for notification queue.
     */
    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(NOTIFICATION_ROUTING_KEY);
    }

    /**
     * Message converter for JSON serialization.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate with JSON converter.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
