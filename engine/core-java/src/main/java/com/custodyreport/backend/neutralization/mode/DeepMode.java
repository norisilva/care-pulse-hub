package com.custodyreport.backend.neutralization.mode;

import com.custodyreport.backend.neutralization.NeutralizationRequest;
import com.custodyreport.backend.neutralization.NeutralizationResult;
import com.custodyreport.backend.neutralization.Severity;
import com.custodyreport.backend.neutralization.Suggestion;
import com.custodyreport.backend.neutralization.pipeline.PipelineContext;
import com.custodyreport.backend.neutralization.steps.PatternMatcherStep;
import com.custodyreport.backend.neutralization.steps.PosTaggingStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deep Mode: reutiliza todos os steps do FastMode e adiciona
 * POS Tagging + Pattern Matching para detecção de construções gramaticais.
 *
 * Se os modelos NLP não estiverem disponíveis, os steps extras são
 * silenciosamente ignorados e o resultado é equivalente ao FastMode.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeepMode implements NeutralizationMode {

    private final FastMode fastMode;
    private final PosTaggingStep posTagging;
    private final PatternMatcherStep patternMatcher;

    @Override
    public NeutralizationResult analyze(NeutralizationRequest request) {
        long start = System.currentTimeMillis();

        // Reutiliza a infraestrutura do FastMode
        PipelineContext ctx = fastMode.buildContext(request);
        fastMode.executeSteps(ctx);

        // Steps adicionais do DeepMode (condicionais)
        posTagging.execute(ctx);
        patternMatcher.execute(ctx);

        // Aplica auto-substituição para sugestões HIGH
        String neutralizedText = applyHighSeveritySuggestions(ctx);

        return new NeutralizationResult(
            request.text(),
            neutralizedText,
            ctx.getSuggestions(),
            ctx.getAppliedSteps(),
            calculateScore(ctx.getSuggestions()),
            calculateScoreByCategory(ctx.getSuggestions()),
            System.currentTimeMillis() - start,
            "DEEP"
        );
    }

    /**
     * Aplica automaticamente as sugestões de severidade HIGH.
     * Sugestões MEDIUM e LOW são apenas apresentadas ao usuário.
     */
    private String applyHighSeveritySuggestions(PipelineContext ctx) {
        String text = ctx.getOriginalText();

        // Ordenar sugestões por offset decrescente para substituir de trás pra frente
        // (evita invalidar offsets das substituições anteriores)
        List<Suggestion> highSuggestions = ctx.getSuggestions().stream()
            .filter(s -> s.severity() == Severity.HIGH || s.severity() == Severity.CRITICAL)
            .filter(s -> s.startOffset() >= 0 && s.endOffset() <= text.length())
            .sorted((a, b) -> Integer.compare(b.startOffset(), a.startOffset()))
            .toList();

        if (highSuggestions.isEmpty()) {
            return null; // Sem reescrita necessária
        }

        StringBuilder result = new StringBuilder(text);
        for (Suggestion s : highSuggestions) {
            if (s.suggestedReplacement() != null && !s.suggestedReplacement().isEmpty()) {
                result.replace(s.startOffset(), s.endOffset(), s.suggestedReplacement());
            }
        }

        return result.toString();
    }

    private double calculateScore(List<Suggestion> suggestions) {
        if (suggestions.isEmpty()) return 0.0;

        double totalWeight = suggestions.stream()
            .mapToDouble(s -> severityWeight(s.severity()))
            .sum();

        // Escala: 5 achados HIGH = score 1.0; proporcional abaixo disso
        double maxScale = 5.0;
        return Math.min(1.0, totalWeight / maxScale);
    }

    private Map<String, Double> calculateScoreByCategory(List<Suggestion> suggestions) {
        Map<String, Double> weights = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>();

        for (Suggestion s : suggestions) {
            String category = extractCategory(s.ruleId());
            weights.merge(category, severityWeight(s.severity()), Double::sum);
            counts.merge(category, 1, Integer::sum);
        }

        // Normalizar: cada categoria escalada por maxScale relativo aos seus achados
        double maxScale = 5.0;
        Map<String, Double> scores = new HashMap<>();
        weights.forEach((cat, total) ->
            scores.put(cat, Math.min(1.0, total / maxScale))
        );
        return scores;
    }

    private double severityWeight(Severity severity) {
        return switch (severity) {
            case CRITICAL -> 1.5;
            case HIGH     -> 1.0;
            case MEDIUM   -> 0.6;
            case LOW      -> 0.3;
        };
    }

    /**
     * Extrai categoria do ruleId. Ex: "ABS_001" → "absolutism",
     * "ACCUSATORY_PATTERN_001" → "accusatory_pattern"
     */
    private String extractCategory(String ruleId) {
        if (ruleId == null) return "unknown";
        // Remove o sufixo numérico
        int lastUnderscore = ruleId.lastIndexOf('_');
        if (lastUnderscore > 0) {
            String prefix = ruleId.substring(0, lastUnderscore).toLowerCase();
            return prefix;
        }
        return ruleId.toLowerCase();
    }
}
