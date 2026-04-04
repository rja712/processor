package com.inboxintelligence.processor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq.embedding")
public record EmbeddingQueueProperties(
        String queue,
        String routingKey
) {
}
