package com.inboxintelligence.processor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "embedding")
public record EmbeddingProperties(
        String provider,
        String ollamaUrl,
        String model
) {
}
