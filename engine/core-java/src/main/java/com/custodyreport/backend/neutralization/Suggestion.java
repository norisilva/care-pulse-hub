package com.custodyreport.backend.neutralization;

public record Suggestion(
    int startOffset,
    int endOffset,
    String originalSpan,
    String suggestedReplacement,
    String reason,
    Severity severity,
    String detectedBy,
    String ruleId
) {}
