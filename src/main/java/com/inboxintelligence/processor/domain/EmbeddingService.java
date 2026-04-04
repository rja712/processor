package com.inboxintelligence.processor.domain;

import com.inboxintelligence.persistence.model.entity.EmailContent;
import com.inboxintelligence.persistence.service.EmailContentService;
import com.inboxintelligence.persistence.storage.EmailStorageProviderFactory;
import com.inboxintelligence.processor.domain.embedding.EmbeddingProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

import static com.inboxintelligence.persistence.model.ProcessedStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final EmailContentService emailContentService;
    private final EmbeddingProviderFactory embeddingProviderFactory;
    private final EmailStorageProviderFactory storageProviderFactory;

    public void generateEmbedding(Long emailContentId) {

        var emailContent = emailContentService
                .findById(emailContentId)
                .orElseThrow(() -> new RuntimeException("EmailContent not found for id: " + emailContentId));

        try {
            emailContent.setProcessedStatus(EMBEDDING_GENERATED);
            emailContentService.save(emailContent);

            String cleanedText = readProcessedContent(emailContent);

            if (!StringUtils.hasText(cleanedText)) {
                log.warn("No processed content found for email [id={}], skipping embedding", emailContentId);
                emailContent.setProcessedStatus(EMBEDDING_GENERATED);
                emailContentService.save(emailContent);
                return;
            }

            List<Double> embedding = embeddingProviderFactory.getProvider().generateEmbedding(cleanedText);
            log.info("EmailContent [id={}] embedding generated [dimensions={}]", emailContentId, embedding.size());

            emailContent.setProcessedStatus(EMBEDDING_GENERATED);
            emailContentService.save(emailContent);

        } catch (Exception e) {

            log.error("Failed to generate embedding for emailContent [id={}]", emailContentId, e);

            emailContent.setProcessedStatus(PROCESSING_FAILED);
            emailContentService.save(emailContent);
        }
    }

    private String readProcessedContent(EmailContent emailContent) {

        String processedContentPath = emailContent.getProcessedContentPath();

        if (!StringUtils.hasText(processedContentPath)) {
            return "";
        }

        var provider = storageProviderFactory.getProvider();
        return Objects.requireNonNullElse(provider.readContent(processedContentPath), "");
    }
}
