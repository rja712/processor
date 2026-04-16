package com.inboxintelligence.processor.domain.embedding.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BedrockEmbeddingProvider implements EmbeddingProvider {

    @Override
    public float[] generateEmbedding(String text) {
        log.warn("BedrockEmbeddingProvider is not yet implemented");
        throw new UnsupportedOperationException("BedrockEmbeddingProvider is not yet implemented");
    }
}
