package com.custodyreport.backend.neutralization.steps;

import com.custodyreport.backend.neutralization.dictionary.SentimentDictionary;
import com.custodyreport.backend.neutralization.pipeline.PipelineContext;
import com.custodyreport.backend.neutralization.pipeline.PipelineStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LuceneStemmerStep implements PipelineStep {

    private final SentimentDictionary dictionary;

    @Override
    public void execute(PipelineContext ctx) {
        List<String> allStems = new ArrayList<>();

        if (ctx.getSentences() != null) {
            for (String sentence : ctx.getSentences()) {
                String[] tokens = sentence.split("\\s+");
                for (String token : tokens) {
                    String clean = token.replaceAll("[^\\p{L}\\p{N}]", "").toLowerCase();
                    if (!clean.isEmpty()) {
                        try {
                            allStems.add(dictionary.stemToken(clean));
                        } catch (IOException e) {
                            log.warn("Failed to stem token: {}", clean, e);
                            allStems.add(clean); // fallback to clean token
                        }
                    } else {
                        allStems.add("");
                    }
                }
            }
        }

        ctx.setStems(allStems);
        ctx.markStepApplied("LuceneStemmer");
    }
}
