package com.custodyreport.backend.neutralization;

public record NeutralizationRequest(
    String text,
    String context,
    String language,
    ModeType mode
) {
    public NeutralizationRequest withMode(ModeType newMode) {
        return new NeutralizationRequest(this.text, this.context, this.language, newMode);
    }
}
