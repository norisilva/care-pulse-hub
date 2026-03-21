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
    
    // As listas não possuem mais @Setter direto para evitar mutações acidentais por outros steps
    private List<String> sentences;
    private List<String> stems;
    private List<String> posTags;
    
    private final List<Suggestion> suggestions = new ArrayList<>();
    private final List<String> appliedSteps = new ArrayList<>();

    public PipelineContext(String originalText, String contextType) {
        this.originalText = originalText;
        this.workingText = originalText;
        this.contextType = contextType;
    }

    // --- Acesso Seguro às Listas ---

    public List<String> getSentences() {
        return sentences == null ? List.of() : java.util.Collections.unmodifiableList(sentences);
    }

    public void setSentences(List<String> sentences) {
        this.sentences = sentences != null ? new ArrayList<>(sentences) : null;
    }

    public List<String> getStems() {
        return stems == null ? List.of() : java.util.Collections.unmodifiableList(stems);
    }

    public void setStems(List<String> stems) {
        this.stems = stems != null ? new ArrayList<>(stems) : null;
    }

    public List<String> getPosTags() {
        return posTags == null ? List.of() : java.util.Collections.unmodifiableList(posTags);
    }

    public void setPosTags(List<String> posTags) {
        this.posTags = posTags != null ? new ArrayList<>(posTags) : null;
    }

    public List<Suggestion> getSuggestions() {
        return java.util.Collections.unmodifiableList(suggestions);
    }

    public List<String> getAppliedSteps() {
        return java.util.Collections.unmodifiableList(appliedSteps);
    }

    public void addSuggestion(Suggestion s) {
        this.suggestions.add(s);
    }

    public void markStepApplied(String stepName) {
        this.appliedSteps.add(stepName);
    }
}
