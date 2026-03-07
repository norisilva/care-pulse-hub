package com.custodyreport.backend.neutralization;

public record CapabilitiesResponse(
    boolean fastModeAvailable,
    boolean nlpModelAvailable,
    boolean semanticAvailable,
    boolean llmAvailable,
    long availableRamMb,
    String activeProfile
) {}
