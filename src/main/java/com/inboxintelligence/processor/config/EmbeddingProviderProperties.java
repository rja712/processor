package com.inboxintelligence.processor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "embedding-provider")
public record EmbeddingProviderProperties(
        String name,
        String ollamaUrl,
        String model,
        Integer numCtx,
        Integer maxChars
) {
}
