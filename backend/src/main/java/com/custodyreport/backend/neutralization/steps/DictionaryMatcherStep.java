package com.custodyreport.backend.neutralization.steps;

import com.custodyreport.backend.neutralization.Suggestion;
import com.custodyreport.backend.neutralization.dictionary.DictionaryEntry;
import com.custodyreport.backend.neutralization.dictionary.SentimentDictionary;
import com.custodyreport.backend.neutralization.pipeline.PipelineContext;
import com.custodyreport.backend.neutralization.pipeline.PipelineStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class DictionaryMatcherStep implements PipelineStep {

    private final SentimentDictionary dictionary;

    @Override
    public void execute(PipelineContext ctx) {
        if (ctx.getSentences() == null) return;
        
        String originalText = ctx.getOriginalText();
        int currentSentenceSearchIndex = 0;

        // 1. Stem Matching
        for (String sentence : ctx.getSentences()) {
            int sentenceStart = originalText.indexOf(sentence, currentSentenceSearchIndex);
            if (sentenceStart == -1) sentenceStart = currentSentenceSearchIndex;
            else currentSentenceSearchIndex = sentenceStart + sentence.length();

            int currentTokenSearchIndex = sentenceStart;
            String[] tokens = sentence.split("\\s+");

            for (String token : tokens) {
                if (token.trim().isEmpty()) continue;
                
                String clean = token.replaceAll("[^\\p{L}\\p{N}]", "").toLowerCase();
                if (clean.isEmpty()) continue;

                int startPos = originalText.indexOf(token, currentTokenSearchIndex);
                if (startPos == -1) {
                    continue; // Skip if exact token cannot be mapped nicely
                }
                currentTokenSearchIndex = startPos + token.length();

                final int finalStartPos = startPos;
                final String finalToken = token;

                try {
                    String stem = dictionary.stemToken(clean);
                    dictionary.findByStem(stem).ifPresent(entry -> {
                        String primarySuggestion = entry.suggestions() != null && !entry.suggestions().isEmpty() 
                            ? entry.suggestions().get(0) 
                            : "[sugestão neutra indisponível]";
                        
                        ctx.addSuggestion(new Suggestion(
                            finalStartPos,
                            finalStartPos + finalToken.length(),
                            finalToken,
                            primarySuggestion,
                            entry.reason(),
                            entry.severity(),
                            "dictionary",
                            entry.ruleId()
                        ));
                    });
                } catch (IOException e) {
                    // Ignora erro de stemming silenciosamente mas loga em trace se necessario
                }
            }
        }

        // 2. Regex Matching
        for (Map.Entry<Pattern, DictionaryEntry> entryMap : dictionary.getRegexIndex().entrySet()) {
            Pattern pattern = entryMap.getKey();
            Matcher matcher = pattern.matcher(originalText);
            
            while (matcher.find()) {
                DictionaryEntry entry = entryMap.getValue();
                String primarySuggestion = entry.suggestions() != null && !entry.suggestions().isEmpty() 
                            ? entry.suggestions().get(0) 
                            : "[revisar formatação]";
                            
                ctx.addSuggestion(new Suggestion(
                    matcher.start(),
                    matcher.end(),
                    matcher.group(),
                    primarySuggestion,
                    entry.reason(),
                    entry.severity(),
                    "regex_pattern",
                    entry.ruleId()
                ));
            }
        }

        ctx.markStepApplied("DictionaryMatcher");
    }
}
