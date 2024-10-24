package com.auction.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {
    @Bean
    public CustomExchange auctionExchange() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-delayed-type", "direct");
        return new CustomExchange("exchange.auction", "x-delayed-message", true, false, arguments);
    }

    @Bean
    public Queue auctionQueue() {
        return QueueBuilder.durable("auction.queue")
                .deadLetterExchange("auction.dlx")
                .build();
    }

    @Bean
    public Binding auctionBinding(Queue auctionQueue, CustomExchange auctionExchange) {
        return BindingBuilder
                .bind(auctionQueue)
                .to(auctionExchange)
                .with("auction")
                .noargs();
    }

    @Bean
    TopicExchange refundExchange() {
        return new TopicExchange("exchange.refund");
    }

    @Bean
    Queue refundQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-queue-mode", "lazy");
        return new Queue("refund.queue", false, false, false, arguments);
    }

    @Bean
    Binding refundBinding(TopicExchange refundExchange, Queue refundQueue) {
        return BindingBuilder
                .bind(refundQueue)
                .to(refundExchange)
                .with("refund.*");
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("auction.dlq").build();
    }

    @Bean
    public FanoutExchange deadLetterExchange() {
        return ExchangeBuilder.fanoutExchange("auction.dlx").build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange());
    }
}
