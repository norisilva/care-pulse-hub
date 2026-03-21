package com.custodyreport.backend.neutralization.steps;

import com.custodyreport.backend.neutralization.Suggestion;
import com.custodyreport.backend.neutralization.dictionary.DictionaryEntry;
import com.custodyreport.backend.neutralization.dictionary.SentimentDictionary;
import com.custodyreport.backend.neutralization.pipeline.PipelineContext;
import com.custodyreport.backend.neutralization.pipeline.PipelineStep;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FuzzyMatcherStep implements PipelineStep {

    private final SentimentDictionary dictionary;
    private final JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();

    @org.springframework.beans.factory.annotation.Value("${carepulse.ai.fuzzy-threshold:0.90}")
    private double threshold;
    
    private static final int MIN_TOKEN_LENGTH = 5;

    @Override
    public void execute(PipelineContext ctx) {
        if (ctx.getSentences() == null) return;
        
        Set<String> alreadyMatched = ctx.getSuggestions().stream()
                .map(Suggestion::originalSpan)
                .collect(Collectors.toSet());

        String originalText = ctx.getOriginalText();
        int currentSentenceSearchIndex = 0;

        for (String sentence : ctx.getSentences()) {
            int sentenceStart = originalText.indexOf(sentence, currentSentenceSearchIndex);
            if (sentenceStart == -1) sentenceStart = currentSentenceSearchIndex;
            else currentSentenceSearchIndex = sentenceStart + sentence.length();

            int currentTokenSearchIndex = sentenceStart;
            for (String token : sentence.split("\\s+")) {
                if (token.trim().isEmpty()) continue;
                
                String clean = token.replaceAll("[^\\p{L}\\p{N}]", "").toLowerCase();
                
                int startPos = originalText.indexOf(token, currentTokenSearchIndex);
                if (startPos == -1) {
                    continue; // Skip if exact token cannot be mapped
                }
                currentTokenSearchIndex = startPos + token.length();

                if (clean.length() < MIN_TOKEN_LENGTH) continue;
                if (alreadyMatched.contains(token)) continue;

                for (var mapEntry : dictionary.getAllOriginals().entrySet()) {
                    String original = mapEntry.getKey();
                    DictionaryEntry dictEntry = mapEntry.getValue();
                    
                    if (original.length() < MIN_TOKEN_LENGTH) continue;

                    double score = jaroWinkler.apply(clean, original);
                    if (score >= threshold) {
                        String primarySuggestion = dictEntry.suggestions() != null && !dictEntry.suggestions().isEmpty() 
                                ? dictEntry.suggestions().get(0) 
                                : "[sugestão indisponível]";
                                
                        ctx.addSuggestion(new Suggestion(
                            startPos,
                            startPos + token.length(),
                            token,
                            primarySuggestion,
                            dictEntry.reason() + " (similaridade: " + String.format("%.0f", score * 100) + "%)",
                            dictEntry.severity(),
                            "fuzzy",
                            dictEntry.ruleId()
                        ));
                        
                        alreadyMatched.add(token);
                        break; // don't match same word multiple times
                    }
                }
            }
        }

        ctx.markStepApplied("FuzzyMatcher");
    }
}
