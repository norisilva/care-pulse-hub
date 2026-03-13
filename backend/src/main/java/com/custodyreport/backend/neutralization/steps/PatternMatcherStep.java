package com.custodyreport.backend.neutralization.steps;

import com.custodyreport.backend.neutralization.Severity;
import com.custodyreport.backend.neutralization.Suggestion;
import com.custodyreport.backend.neutralization.dictionary.GrammarPattern;
import com.custodyreport.backend.neutralization.pipeline.PipelineContext;
import com.custodyreport.backend.neutralization.pipeline.PipelineStep;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Detecta construções gramaticais problemáticas usando sequências de POS tags.
 * Exemplo: "ela nunca leva" = [PRON, ADV, V] → padrão acusatório.
 * Só executa se o PosTaggingStep rodou antes (ctx.getPosTags() preenchido).
 */
@Slf4j
@Component
public class PatternMatcherStep implements PipelineStep {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<GrammarPattern> patterns = new ArrayList<>();

    @PostConstruct
    public void loadPatterns() {
        try (InputStream is = new ClassPathResource("neutralization/patterns-pt-BR.json").getInputStream()) {
            JsonNode root = objectMapper.readTree(is);
            JsonNode patternsNode = root.path("patterns");

            for (JsonNode p : patternsNode) {
                List<String> posSequence = new ArrayList<>();
                for (JsonNode tag : p.path("posSequence")) {
                    posSequence.add(tag.asText().toLowerCase());
                }

                List<String> requiredStems = new ArrayList<>();
                for (JsonNode stem : p.path("requiredStems")) {
                    requiredStems.add(stem.asText().toLowerCase());
                }

                patterns.add(new GrammarPattern(
                    p.path("ruleId").asText(),
                    posSequence,
                    p.path("description").asText(),
                    Severity.valueOf(p.path("severity").asText("HIGH")),
                    p.path("suggestion").asText(),
                    requiredStems
                ));
            }

            log.info("PatternMatcher carregado com {} padrões gramaticais", patterns.size());
        } catch (Exception e) {
            log.warn("Falha ao carregar patterns-pt-BR.json: {} — PatternMatcher desabilitado", e.getMessage());
        }
    }

    @Override
    public boolean shouldExecute(PipelineContext context) {
        return context.getPosTags() != null && !context.getPosTags().isEmpty();
    }

    @Override
    public void execute(PipelineContext ctx) {
        if (!shouldExecute(ctx)) {
            return;
        }

        // Para cada sentença, montar tokens + tags e verificar padrões
        int globalTagIndex = 0;

        for (String sentence : ctx.getSentences()) {
            String[] tokens = sentence.split("\\s+");
            int sentenceTagCount = tokens.length;

            // Extrair tags desta sentença
            List<String> sentenceTags = new ArrayList<>();
            for (int i = 0; i < sentenceTagCount && (globalTagIndex + i) < ctx.getPosTags().size(); i++) {
                sentenceTags.add(ctx.getPosTags().get(globalTagIndex + i).toLowerCase());
            }

            // Janela deslizante para cada padrão
            for (GrammarPattern pattern : patterns) {
                matchPattern(ctx, sentence, tokens, sentenceTags, pattern);
            }

            globalTagIndex += sentenceTagCount;
        }

        ctx.markStepApplied("PatternMatcher");
    }

    /**
     * Verifica se a sequência de POS tags da sentença contém o padrão.
     * Usa janela deslizante do tamanho do padrão.
     */
    private void matchPattern(PipelineContext ctx, String sentence, String[] tokens,
                               List<String> sentenceTags, GrammarPattern pattern) {
        int patternSize = pattern.posSequence().size();

        if (sentenceTags.size() < patternSize) {
            return;
        }

        for (int i = 0; i <= sentenceTags.size() - patternSize; i++) {
            boolean matches = true;

            for (int j = 0; j < patternSize; j++) {
                String expectedTag = pattern.posSequence().get(j);
                String actualTag = sentenceTags.get(i + j);

                if (!tagMatches(expectedTag, actualTag)) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                // Construir o span completo da construção encontrada
                StringBuilder spanBuilder = new StringBuilder();
                int startTokenIndex = i;
                int endTokenIndex = i + patternSize - 1;

                for (int k = startTokenIndex; k <= endTokenIndex && k < tokens.length; k++) {
                    if (k > startTokenIndex) spanBuilder.append(" ");
                    spanBuilder.append(tokens[k]);
                }

                String matchedSpan = spanBuilder.toString();

                // Se o padrão exige certain stems, verificar se pelo menos um está presente
                // Isso evita falsos positivos em padrões genéricos (ex: VERB+ADV dispara em "foi lá")
                if (!pattern.requiredStems().isEmpty()) {
                    String spanLower = matchedSpan.toLowerCase();
                    boolean stemFound = pattern.requiredStems().stream()
                        .anyMatch(spanLower::contains);
                    if (!stemFound) {
                        continue; // Nenhum stem obrigatório presente — não é um match válido
                    }
                }

                int startOffset = ctx.getOriginalText().toLowerCase().indexOf(matchedSpan.toLowerCase());
                if (startOffset < 0) startOffset = 0;
                int endOffset = startOffset + matchedSpan.length();

                ctx.addSuggestion(new Suggestion(
                    startOffset,
                    endOffset,
                    matchedSpan,
                    pattern.suggestion(),
                    pattern.description(),
                    pattern.severity(),
                    "pos_pattern",
                    pattern.ruleId()
                ));

                log.debug("Padrão '{}' detectado: '{}' na sentença: '{}'",
                    pattern.ruleId(), matchedSpan, sentence);
            }
        }
    }

    /**
     * Verifica se uma tag POS real casa com a tag esperada pelo padrão.
     * Tags esperadas podem conter alternativas separadas por '|'.
     * Faz matching por prefixo para flexibilidade (ex: "v" casa com "v-fin", "v-inf").
     */
    private boolean tagMatches(String expected, String actual) {
        // Suporte a alternativas: "PRP|NNP"
        String[] alternatives = expected.split("\\|");

        for (String alt : alternatives) {
            String altLower = alt.trim().toLowerCase();
            String actualLower = actual.toLowerCase();

            // Match por prefixo: "v" casa com "v-fin", "v-inf", "v-pcp", etc.
            if (actualLower.startsWith(altLower) || actualLower.equals(altLower)) {
                return true;
            }
        }

        return false;
    }
}
