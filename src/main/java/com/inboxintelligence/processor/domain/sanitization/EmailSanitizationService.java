package com.inboxintelligence.processor.domain.sanitization;

import com.inboxintelligence.persistence.service.EmailContentService;
import com.inboxintelligence.persistence.storage.EmailStorageProviderFactory;
import com.inboxintelligence.processor.domain.sanitization.pipeline.ContentSanitizationPipelineRegistry;
import com.inboxintelligence.processor.outbound.EmailEmbeddingPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static com.inboxintelligence.persistence.model.ProcessedStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSanitizationService {

    private final EmailEmbeddingPublisher emailEmbeddingPublisher;
    private final EmailContentService emailContentService;
    private final EmailStorageProviderFactory storageProviderFactory;
    private final ContentSanitizationPipelineRegistry pipelineRegistry;

    public void sanitizeEmail(Long emailContentId) {

        log.info("Starting sanitization for email id: {}", emailContentId);
        var emailContent = emailContentService
                .findById(emailContentId)
                .orElseThrow(() -> new RuntimeException("EmailContent not found for id: " + emailContentId));

        try {

            var provider = storageProviderFactory.getProvider();
            emailContentService.updateStatusAndNote(emailContent, SANITIZATION_STARTED, null);

            String html = provider.readContent(emailContent.getBodyHtmlContentPath());
            String body = provider.readContent(emailContent.getBodyContentPath());

            String rawContent = StringUtils.hasText(html) ? html : StringUtils.hasText(body) ? body : "";

            if (!StringUtils.hasText(rawContent)) {
                log.warn("No raw content for email id: {}", emailContentId);
                emailContentService.updateStatusAndNote(emailContent, PROCESSING_FAILED, "No raw content");
                return;
            }

            int originalLength = rawContent.length();
            String cleanedText = pipelineRegistry.executeSanitizationPipeline(rawContent);

            if (cleanedText.length() < 20 && cleanedText.length() < originalLength * 0.1) {
                log.warn("Pipeline removed too much content ({} -> {} chars), falling back to original", originalLength, cleanedText.length());
                cleanedText = rawContent;
            }

            log.info("Sanitized email [id={}, {} -> {} chars]", emailContent.getId(), originalLength, cleanedText.length());

            String sanitizedContentPath = provider.writeContent(emailContent.getGmailMailboxId(), emailContent.getMessageId(), "processed_content.txt", cleanedText);


            emailContent.setSanitizedContentPath(sanitizedContentPath);
            emailContent.setProcessedStatus(SANITIZATION_COMPLETED);
            emailContentService.save(emailContent);

            log.info("Sanitized content stored at: {} for email id: {}", sanitizedContentPath, emailContent.getId());

            emailEmbeddingPublisher.publishEmbeddingEvent(emailContent);
            log.info("EmailContent [id={}] sanitized and queued for embedding", emailContentId);


        } catch (Exception e) {
            log.error("Failed to process emailContent [id={}]", emailContentId, e);
            emailContentService.updateStatusAndNote(emailContent, PROCESSING_FAILED, e.getMessage());
            throw e;
        }
    }


}
