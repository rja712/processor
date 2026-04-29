package com.inboxintelligence.processor.domain.sanitization.pipeline;

public interface SanitizationStepProcessor {
    String process(String content);
}
