package com.inboxintelligence.processor.domain.embedding.factory;

public interface EmbeddingProvider {

    float[] generateEmbedding(String text);
}
