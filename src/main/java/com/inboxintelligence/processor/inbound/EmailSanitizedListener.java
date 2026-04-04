package com.inboxintelligence.processor.inbound;

import com.inboxintelligence.processor.domain.EmbeddingService;
import com.inboxintelligence.processor.model.EmailSanitizedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSanitizedListener {

    private final EmbeddingService embeddingService;

    @RabbitListener(queues = "#{@emailEmbeddingQueue.name}")
    public void handleEmailSanitizedEvent(EmailSanitizedEvent event) {
        log.info("Received EmailSanitizedEvent for emailContentId: {}", event.emailContentId());
        embeddingService.generateEmbedding(event.emailContentId());
    }
}
