package com.custodyreport.backend.neutralization.pipeline;

import com.custodyreport.backend.neutralization.Suggestion;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PipelineContext {
    private final String originalText;
    private final String contextType;
    @Setter private String workingText;
    @Setter private List<String> sentences;
    @Setter private List<String> stems;
    @Setter private List<String> posTags;
    private final List<Suggestion> suggestions = new ArrayList<>();
    private final List<String> appliedSteps = new ArrayList<>();

    public PipelineContext(String originalText, String contextType) {
        this.originalText = originalText;
        this.workingText = originalText;
        this.contextType = contextType;
    }

    public void addSuggestion(Suggestion s) {
        this.suggestions.add(s);
    }

    public void markStepApplied(String stepName) {
        this.appliedSteps.add(stepName);
    }
}
