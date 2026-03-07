package com.custodyreport.backend.neutralization.steps;

import com.custodyreport.backend.neutralization.pipeline.PipelineContext;
import com.custodyreport.backend.neutralization.pipeline.PipelineStep;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SentenceDetectorStep implements PipelineStep {

    // Regex to split by basic sentence terminators
    private static final Pattern SENTENCE_SPLIT_PATTERN = Pattern.compile("[^.!?\\n]+(?:[.!?\\n]+|$)");

    @Override
    public void execute(PipelineContext ctx) {
        List<String> sentences = new ArrayList<>();
        Matcher matcher = SENTENCE_SPLIT_PATTERN.matcher(ctx.getWorkingText());

        while (matcher.find()) {
            String sentence = matcher.group().trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
        }

        ctx.setSentences(sentences);
        ctx.markStepApplied("SentenceDetector");
    }
}
