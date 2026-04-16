package com.inboxintelligence.processor.domain.embedding.factory;

import java.util.List;

public interface EmbeddingProvider {

    List<Double> generateEmbedding(String text);
}
