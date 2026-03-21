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

        PipelineContext ctx = buildContext(request);
        executeSteps(ctx);

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

    /**
     * Cria o PipelineContext a partir da request.
     * Exposto para reutilização pelo DeepMode.
     */
    public PipelineContext buildContext(NeutralizationRequest request) {
        return new PipelineContext(request.text(), request.context());
    }

    /**
     * Executa os 4 steps do FastMode na ordem correta.
     * Exposto para reutilização pelo DeepMode.
     */
    public void executeSteps(PipelineContext ctx) {
        sentenceDetector.execute(ctx);
        stemmer.execute(ctx);
        dictionaryMatcher.execute(ctx);
        fuzzyMatcher.execute(ctx);
    }

    private double calculateScore(List<Suggestion> suggestions) {
        if (suggestions.isEmpty()) return 0.0;

        double totalWeight = suggestions.stream()
            .mapToDouble(s -> switch (s.severity()) {
                case CRITICAL -> 1.5;
                case HIGH     -> 1.0;
                case MEDIUM   -> 0.6;
                case LOW      -> 0.3;
            })
            .sum();

        // Escala: 5 achados HIGH = score 1.0; proporcional abaixo disso
        return Math.min(1.0, totalWeight / 5.0);
    }

    private Map<String, Double> calculateScoreByCategory(List<Suggestion> suggestions) {
        return new HashMap<>(); // A ser implementado com scoring detalhado se necessário
    }
}
