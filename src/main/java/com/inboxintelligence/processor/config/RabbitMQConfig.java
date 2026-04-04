package com.inboxintelligence.processor.config;

import com.inboxintelligence.persistence.config.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final RabbitMQProperties properties;
    private final EmbeddingQueueProperties embeddingQueueProperties;

    // --- Inbound (sanitization queue with DLQ routing) ---

    @Bean
    public Queue emailInboundQueue() {
        return QueueBuilder.durable(properties.queue())
                .withArgument("x-dead-letter-exchange", properties.exchange() + ".dlx")
                .withArgument("x-dead-letter-routing-key", properties.routingKey() + ".dlq")
                .build();
    }

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(properties.exchange());
    }

    @Bean
    public Binding emailInboundBinding(Queue emailInboundQueue, TopicExchange emailExchange) {
        return BindingBuilder
                .bind(emailInboundQueue)
                .to(emailExchange)
                .with(properties.routingKey());
    }

    // --- Embedding (embedding queue with DLQ routing, same exchange) ---

    @Bean
    public Queue emailEmbeddingQueue() {
        return QueueBuilder.durable(embeddingQueueProperties.queue())
                .withArgument("x-dead-letter-exchange", properties.exchange() + ".dlx")
                .withArgument("x-dead-letter-routing-key", embeddingQueueProperties.routingKey() + ".dlq")
                .build();
    }

    @Bean
    public Binding emailEmbeddingBinding(Queue emailEmbeddingQueue, TopicExchange emailExchange) {
        return BindingBuilder
                .bind(emailEmbeddingQueue)
                .to(emailExchange)
                .with(embeddingQueueProperties.routingKey());
    }

    // --- Dead Letter Queue (shared DLX) ---

    @Bean
    public Queue inboundDeadLetterQueue() {
        return QueueBuilder.durable(properties.queue() + ".dlq").build();
    }

    @Bean
    public Queue embeddingDeadLetterQueue() {
        return QueueBuilder.durable(embeddingQueueProperties.queue() + ".dlq").build();
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(properties.exchange() + ".dlx");
    }

    @Bean
    public Binding inboundDeadLetterBinding(Queue inboundDeadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder
                .bind(inboundDeadLetterQueue)
                .to(deadLetterExchange)
                .with(properties.routingKey() + ".dlq");
    }

    @Bean
    public Binding embeddingDeadLetterBinding(Queue embeddingDeadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder
                .bind(embeddingDeadLetterQueue)
                .to(deadLetterExchange)
                .with(embeddingQueueProperties.routingKey() + ".dlq");
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
