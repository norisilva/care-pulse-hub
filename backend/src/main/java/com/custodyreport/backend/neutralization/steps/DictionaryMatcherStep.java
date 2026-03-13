package com.custodyreport.backend.neutralization.steps;

import com.custodyreport.backend.neutralization.Suggestion;
import com.custodyreport.backend.neutralization.dictionary.DictionaryEntry;
import com.custodyreport.backend.neutralization.dictionary.SentimentDictionary;
import com.custodyreport.backend.neutralization.pipeline.PipelineContext;
import com.custodyreport.backend.neutralization.pipeline.PipelineStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
            List<String> tokenStems = new ArrayList<>();
            List<TokenInfo> tokenInfos = new ArrayList<>();

            for (String token : tokens) {
                if (token.trim().isEmpty()) continue;
                
                String clean = token.replaceAll("[^\\p{L}\\p{N}]", "").toLowerCase();
                if (clean.isEmpty()) continue;

                int startPos = originalText.indexOf(token, currentTokenSearchIndex);
                if (startPos == -1) {
                    continue; // Skip if exact token cannot be mapped nicely
                }
                currentTokenSearchIndex = startPos + token.length();

                try {
                    String stem = dictionary.stemToken(clean);
                    tokenStems.add(stem);
                    tokenInfos.add(new TokenInfo(startPos, startPos + token.length(), token, stem));
                } catch (IOException e) {
                    // Ignora erro de stemming silenciosamente
                }
            }

            // 1.1 Phrase Matching (Multistems)
            boolean[] matchedTokens = new boolean[tokenInfos.size()];
            for (Map.Entry<String, DictionaryEntry> phraseEntry : dictionary.getPhraseIndex().entrySet()) {
                String[] phraseParts = phraseEntry.getKey().split("\\s+");
                int phraseLen = phraseParts.length;

                for (int i = 0; i <= tokenInfos.size() - phraseLen; i++) {
                    boolean match = true;
                    for (int j = 0; j < phraseLen; j++) {
                        if (!phraseParts[j].equals(tokenInfos.get(i + j).stem)) {
                            match = false;
                            break;
                        }
                    }

                    if (match) {
                        int startPos = tokenInfos.get(i).startPos;
                        int endPos = tokenInfos.get(i + phraseLen - 1).endPos;
                        String matchedText = originalText.substring(startPos, endPos);
                        
                        DictionaryEntry entry = phraseEntry.getValue();
                        String primarySuggestion = entry.suggestions() != null && !entry.suggestions().isEmpty() 
                            ? entry.suggestions().get(0) 
                            : "[sugestão neutra indisponível]";

                        ctx.addSuggestion(new Suggestion(
                            startPos,
                            endPos,
                            matchedText,
                            primarySuggestion,
                            entry.reason(),
                            entry.severity(),
                            "dictionary_phrase",
                            entry.ruleId()
                        ));

                        for (int j = 0; j < phraseLen; j++) {
                            matchedTokens[i + j] = true;
                        }
                    }
                }
            }

            // 1.2 Single Stem Matching (skip tokens already matched in phrases)
            for (int i = 0; i < tokenInfos.size(); i++) {
                if (matchedTokens[i]) continue;
                
                TokenInfo info = tokenInfos.get(i);
                dictionary.findByStem(info.stem).ifPresent(entry -> {
                    String primarySuggestion = entry.suggestions() != null && !entry.suggestions().isEmpty() 
                        ? entry.suggestions().get(0) 
                        : "[sugestão neutra indisponível]";
                    
                    ctx.addSuggestion(new Suggestion(
                        info.startPos,
                        info.endPos,
                        info.original,
                        primarySuggestion,
                        entry.reason(),
                        entry.severity(),
                        "dictionary",
                        entry.ruleId()
                    ));
                });
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

    private record TokenInfo(int startPos, int endPos, String original, String stem) {}
}
