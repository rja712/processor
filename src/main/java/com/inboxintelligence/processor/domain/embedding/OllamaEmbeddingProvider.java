package com.inboxintelligence.processor.domain.embedding;

import com.inboxintelligence.processor.config.EmbeddingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OllamaEmbeddingProvider implements EmbeddingProvider {

    private final RestClient restClient;
    private final EmbeddingProperties properties;

    @Override
    public List<Double> generateEmbedding(String text) {

        Map<String, String> request = Map.of(
                "model", properties.model(),
                "prompt", text
        );

        log.debug("Requesting embedding from Ollama [model={}, textLength={}]", properties.model(), text.length());

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .uri(properties.ollamaUrl())
                .body(request)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("embedding")) {
            throw new IllegalStateException("Ollama returned no embedding");
        }

        @SuppressWarnings("unchecked")
        List<Double> embedding = (List<Double>) response.get("embedding");

        log.info("Generated embedding [model={}, dimensions={}]", properties.model(), embedding.size());
        return embedding;
    }
}
