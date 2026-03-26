package com.inboxintelligence.processor.domain;

import com.inboxintelligence.processor.domain.sanitization.ContentSanitizationPipelineRegistry;
import com.inboxintelligence.processor.model.ProcessedStatus;
import com.inboxintelligence.processor.model.entity.EmailContent;
import com.inboxintelligence.processor.persistence.service.EmailContentService;
import com.inboxintelligence.processor.persistence.storage.EmailStorageReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProcessingService {

    private final EmailContentService emailContentService;

    private final EmailStorageReader emailStorageReader;
    private final ContentSanitizationPipelineRegistry pipelineRegistry;


    public void processEmail(Long emailContentId) {

        var email = getEmailContentAndUpdateStatus(emailContentId);

        if (email != null) {
            String cleanedText = triggerSanitizationPipeline(email);
        }

        // TODO: Pass cleanedText to Intelligence Layer (LLM)
    }

    @Transactional
    public EmailContent getEmailContentAndUpdateStatus(Long emailContentId) {

        var emailContentOptional = emailContentService.findById(emailContentId);

        if (emailContentOptional.isEmpty()) {
            log.warn("EmailContent not found for id: {}", emailContentId);
            return null;
        }

        var email = emailContentOptional.get();
        log.info("Processing email [id={}, subject='{}']", email.getId(), email.getSubject());

        email.setProcessedStatus(ProcessedStatus.PROCESSING_STARTED);
        return emailContentService.save(email);
    }

    public String triggerSanitizationPipeline(EmailContent email) {

        String bodyContent = emailStorageReader.readContent(email.getBodyContentPath()).orElse("");
        String htmlContent = emailStorageReader.readContent(email.getBodyHtmlContentPath()).orElse("");
        log.debug("Content for email [id={}]: body={} chars, html={} chars", email.getId(), bodyContent.length(), htmlContent.length());

        String rawContent = "";

        if (StringUtils.hasText(htmlContent)) {
            rawContent = htmlContent;
        } else if (StringUtils.hasText(bodyContent)) {
            rawContent = bodyContent;
        } else {
            return "";
        }


        int originalLength = rawContent.length();
        String cleanedText = pipelineRegistry.executeSanitizationPipeline(rawContent);

        if (cleanedText.length() < 20 && cleanedText.length() < originalLength * 0.1) {
            log.warn("Pipeline removed too much content ({} -> {} chars), falling back to original", originalLength, cleanedText.length());
            return rawContent;
        }


        log.info("Cleaned email [id={}, {} -> {} chars]", email.getId(), htmlContent.length() + bodyContent.length(), cleanedText.length());

        return cleanedText;
    }


}
