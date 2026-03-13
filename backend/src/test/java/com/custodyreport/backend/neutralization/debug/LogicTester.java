package com.custodyreport.backend.neutralization.debug;

import com.custodyreport.backend.neutralization.Severity;
import com.custodyreport.backend.neutralization.Suggestion;
import com.custodyreport.backend.neutralization.dictionary.GrammarPattern;
import com.custodyreport.backend.neutralization.pipeline.PipelineContext;
import com.custodyreport.backend.neutralization.steps.PatternMatcherStep;
import java.util.List;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class LogicTester {
    public static void main(String[] args) throws Exception {
        PatternMatcherStep step = new PatternMatcherStep();
        
        // Manual setup of patterns to avoid file loading issues in this static test
        Field patternsField = PatternMatcherStep.class.getDeclaredField("patterns");
        patternsField.setAccessible(true);
        List<GrammarPattern> patterns = (List<GrammarPattern>) patternsField.get(step);
        
        patterns.add(new GrammarPattern(
            "ACCUSATORY_PATTERN_001",
            List.of("pron", "adv", "verb"),
            "Construção acusatória",
            Severity.HIGH,
            "Reformule",
            List.of()
        ));
        
        PipelineContext ctx = new PipelineContext("Ele frequentemente esquece de trazer o casaco da criança.", "custody");
        ctx.setSentences(List.of("Ele frequentemente esquece de trazer o casaco da criança."));
        ctx.setPosTags(List.of("PRON", "ADV", "VERB", "SCONJ", "VERB", "DET", "NOUN", "ADP+DET", "NOUN"));
        
        step.execute(ctx);
        
        System.out.println("Suggestions found: " + ctx.getSuggestions().size());
        for (Suggestion s : ctx.getSuggestions()) {
            System.out.println(" - " + s.ruleId() + ": " + s.originalSpan());
        }
    }
}
