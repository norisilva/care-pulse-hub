package com.custodyreport.backend.neutralization;

import java.util.List;
import java.util.Map;

public record NeutralizationResult(
    String originalText,
    String neutralizedText,
    List<Suggestion> suggestions,
    List<String> appliedSteps,
    double overallScore,
    Map<String, Double> scoreByCategory,
    long processingTimeMs,
    String modeUsed
) {}
