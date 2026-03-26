package com.inboxintelligence.processor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "sanitization")
public record SanitizationPatternProperties(
        List<String> quotedTextPatterns,
        List<String> signaturePatterns,
        List<String> signOffPhrases,
        List<String> disclaimerKeywords
) {
}
