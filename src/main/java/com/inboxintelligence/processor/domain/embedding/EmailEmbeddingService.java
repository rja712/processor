package com.inboxintelligence.processor.domain.embedding;

import com.inboxintelligence.persistence.model.entity.EmailContent;
import com.inboxintelligence.persistence.service.EmailContentService;
import com.inboxintelligence.persistence.storage.EmailStorageProviderFactory;
import com.inboxintelligence.processor.config.EmbeddingProviderProperties;
import com.inboxintelligence.processor.domain.embedding.factory.EmbeddingProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static com.inboxintelligence.persistence.model.ProcessedStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailEmbeddingService {

    private final EmailContentService emailContentService;
    private final EmbeddingProviderFactory embeddingProviderFactory;
    private final EmailStorageProviderFactory storageProviderFactory;
    private final EmbeddingProviderProperties embeddingProviderProperties;

    public void generateEmbedding(Long emailContentId) {

        log.info("Starting embedding for emailContent id: {}", emailContentId);
        var emailContent = emailContentService
                .findById(emailContentId)
                .orElseThrow(() -> new IllegalStateException("EmailContent not found: " + emailContentId));

        try {

            var storageProvider = storageProviderFactory.getProvider();
            var embeddingProvider = embeddingProviderFactory.getProvider();

            emailContentService.updateStatusAndNote(emailContent, EMBEDDING_STARTED, null);

            String sanitizedContent = storageProvider.readContent(emailContent.getSanitizedContentPath());

            if (!StringUtils.hasText(sanitizedContent)){
                log.warn("No sanitized content for emailContent [id={}], marking failed", emailContentId);
                emailContentService.updateStatusAndNote(emailContent, PROCESSING_FAILED, "No sanitized content found");
                return;
            }

            float[] embedding = embeddingProvider.generateEmbedding(sanitizedContent);
            log.info("EmailContent [id={}] embedding generated [dimensions={}]", emailContentId, embedding.length);

            emailContent.setEmbedding(embedding);
            emailContent.setEmbeddingModel(embeddingProviderProperties.model());
            emailContent.setProcessedStatus(EMBEDDING_GENERATED);
            emailContentService.save(emailContent);
            log.info("EmailContent [id={}] embedding persisted [model={}]", emailContentId, embeddingProviderProperties.model());

        } catch (Exception e) {
            log.error("Failed to embed emailContent [id={}]", emailContentId, e);
            emailContentService.updateStatusAndNote(emailContent, PROCESSING_FAILED, e.getMessage());
            throw e;
        }
    }

}
