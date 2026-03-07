package com.custodyreport.backend.neutralization.mode;

import com.custodyreport.backend.neutralization.NeutralizationRequest;
import com.custodyreport.backend.neutralization.NeutralizationResult;
import com.custodyreport.backend.neutralization.Suggestion;
import com.custodyreport.backend.neutralization.Severity;
import com.custodyreport.backend.neutralization.pipeline.PipelineContext;
import com.custodyreport.backend.neutralization.steps.DictionaryMatcherStep;
import com.custodyreport.backend.neutralization.steps.FuzzyMatcherStep;
import com.custodyreport.backend.neutralization.steps.LuceneStemmerStep;
import com.custodyreport.backend.neutralization.steps.SentenceDetectorStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FastMode implements NeutralizationMode {

    private final SentenceDetectorStep sentenceDetector;
    private final LuceneStemmerStep stemmer;
    private final DictionaryMatcherStep dictionaryMatcher;
    private final FuzzyMatcherStep fuzzyMatcher;

    @Override
    public NeutralizationResult analyze(NeutralizationRequest request) {
        long start = System.currentTimeMillis();

        PipelineContext ctx = new PipelineContext(request.text(), request.context());

        sentenceDetector.execute(ctx);
        stemmer.execute(ctx);
        dictionaryMatcher.execute(ctx);
        fuzzyMatcher.execute(ctx);

        return new NeutralizationResult(
            request.text(),
            null, // FastMode does not rewrite text
            ctx.getSuggestions(),
            ctx.getAppliedSteps(),
            calculateScore(ctx.getSuggestions()),
            calculateScoreByCategory(ctx.getSuggestions()),
            System.currentTimeMillis() - start,
            "FAST"
        );
    }

    private double calculateScore(List<Suggestion> suggestions) {
        if (suggestions.isEmpty()) return 0.0;
        
        double totalScore = 0;
        for (Suggestion s : suggestions) {
            totalScore += switch (s.severity()) {
                case HIGH -> 1.0;
                case MEDIUM -> 0.6;
                case LOW -> 0.3;
            };
        }
        
        // Return average score per suggestion, capped at 1.0
        return Math.min(1.0, totalScore / Math.max(1, suggestions.size()));
    }

    private Map<String, Double> calculateScoreByCategory(List<Suggestion> suggestions) {
        return new HashMap<>(); // To be implemented with detailed category scoring if needed
    }
}
