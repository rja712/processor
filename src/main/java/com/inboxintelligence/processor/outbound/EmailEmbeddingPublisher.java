package com.inboxintelligence.processor.outbound;

import com.inboxintelligence.processor.config.EmailEventRabbitMQProperties;
import com.inboxintelligence.processor.model.EmailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEmbeddingPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final EmailEventRabbitMQProperties properties;

    public void publishEmbeddingEvent(Long emailContentId) {

        rabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.embeddingRoutingKey(),
                new EmailEvent(emailContentId)
        );
        log.debug("Published EmailSanitizedEvent for emailContentId: {}", emailContentId);
    }
}
