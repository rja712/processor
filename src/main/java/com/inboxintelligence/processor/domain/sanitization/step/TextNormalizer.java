package com.inboxintelligence.processor.domain.sanitization.step;

import com.inboxintelligence.processor.config.SanitizationStep;

import java.util.Map;
import java.util.regex.Pattern;

@SanitizationStep(order = 4, description = "Normalize fancy/unicode characters into simple LLM-friendly text")
public class TextNormalizer {

    // Smart quotes → simple quotes
    private static final Map<String, String> SMART_QUOTE_MAP = Map.of(
            "\u2018", "'",   // left single quote '
            "\u2019", "'",   // right single quote '
            "\u201A", "'",   // single low quote ‚
            "\u201C", "\"",  // left double quote "
            "\u201D", "\"",  // right double quote "
            "\u201E", "\"",  // double low quote „
            "\u2039", "'",   // single left angle ‹
            "\u203A", "'",   // single right angle ›
            "\u00AB", "\"",  // left double angle «
            "\u00BB", "\""   // right double angle »
    );

    // Dashes → simple dash
    private static final Map<String, String> DASH_MAP = Map.of(
            "\u2013", "-",   // en dash –
            "\u2014", "-",   // em dash —
            "\u2015", "-",   // horizontal bar ―
            "\u2012", "-",   // figure dash ‒
            "\u2010", "-",   // hyphen ‐
            "\u2011", "-"    // non-breaking hyphen ‑
    );

    // Bullets and list markers → simple dash
    private static final Map<String, String> BULLET_MAP = Map.of(
            "\u2022", "-",   // bullet •
            "\u2023", "-",   // triangular bullet ‣
            "\u25E6", "-",   // white bullet ◦
            "\u25AA", "-",   // black small square ▪
            "\u25CF", "-",   // black circle ●
            "\u2043", "-"    // hyphen bullet ⁃
    );

    // Arrows → simple text
    private static final Map<String, String> ARROW_MAP = Map.of(
            "\u2192", "->",  // rightwards arrow →
            "\u2190", "<-",  // leftwards arrow ←
            "\u2194", "<->", // left right arrow ↔
            "\u21D2", "=>",  // rightwards double arrow ⇒
            "\u21D0", "<="   // leftwards double arrow ⇐
    );

    // Misc symbols → text
    private static final Map<String, String> SYMBOL_MAP = Map.of(
            "\u2026", "...", // ellipsis …
            "\u00A9", "(c)", // copyright ©
            "\u00AE", "(R)", // registered ®
            "\u2122", "(TM)",// trademark ™
            "\u00B0", " degrees", // degree °
            "\u00D7", "x",  // multiplication ×
            "\u00F7", "/"   // division ÷
    );

    // Unicode spaces → normal space
    private static final Pattern UNICODE_SPACES = Pattern.compile(
            "[\u00A0\u2007\u202F\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2008\u2009\u200A\u205F\u3000]"
    );

    // Zero-width / invisible characters → remove
    private static final Pattern INVISIBLE_CHARS = Pattern.compile(
            "[\u200B\u200C\u200D\u200E\u200F\uFEFF\u00AD\u034F\u061C\u2060\u2061\u2062\u2063\u2064]"
    );

    // Excessive newlines → max two
    private static final Pattern EXCESSIVE_NEWLINES = Pattern.compile("\n{3,}");

    // Trailing spaces on each line
    private static final Pattern TRAILING_SPACES = Pattern.compile("(?m)[ \\t]+$");

    public String process(String content) {

        String result = content;

        // Normalize line endings
        result = result.replace("\r\n", "\n");
        result = result.replace("\r", "\n");

        // Replace all fancy characters with simple equivalents
        result = applyReplacements(result, SMART_QUOTE_MAP);
        result = applyReplacements(result, DASH_MAP);
        result = applyReplacements(result, BULLET_MAP);
        result = applyReplacements(result, ARROW_MAP);
        result = applyReplacements(result, SYMBOL_MAP);

        // Normalize spaces and invisible characters
        result = UNICODE_SPACES.matcher(result).replaceAll(" ");
        result = INVISIBLE_CHARS.matcher(result).replaceAll("");

        // Clean up whitespace
        result = TRAILING_SPACES.matcher(result).replaceAll("");
        result = EXCESSIVE_NEWLINES.matcher(result).replaceAll("\n\n");

        return result.strip();
    }

    private String applyReplacements(String text, Map<String, String> replacements) {
        for (var entry : replacements.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }
}
