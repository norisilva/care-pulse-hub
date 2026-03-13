package com.custodyreport.backend.neutralization.mode;

import com.custodyreport.backend.neutralization.ModeType;
import com.custodyreport.backend.neutralization.NeutralizationRequest;
import com.custodyreport.backend.neutralization.NeutralizationResult;
import com.custodyreport.backend.neutralization.Severity;
import com.custodyreport.backend.neutralization.Suggestion;
import com.custodyreport.backend.neutralization.pipeline.PipelineContext;
import com.custodyreport.backend.neutralization.steps.PatternMatcherStep;
import com.custodyreport.backend.neutralization.steps.PosTaggingStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeepModeTest {

    @Mock
    private FastMode fastMode;

    @Mock
    private PosTaggingStep posTagging;

    @Mock
    private PatternMatcherStep patternMatcher;

    @InjectMocks
    private DeepMode deepMode;

    private PipelineContext context;

    @BeforeEach
    void setUp() {
        context = new PipelineContext("Original text", "EMAIL");
        lenient().when(fastMode.buildContext(any(NeutralizationRequest.class))).thenReturn(context);
    }

    @Test
    void shouldExecuteAllStepsAndReturnDeepResult() {
        NeutralizationRequest req = new NeutralizationRequest("Original text", "EMAIL", "pt-BR", ModeType.DEEP);

        // Simulate PatternMatcher adding a suggestion
        doAnswer(invocation -> {
            PipelineContext ctx = invocation.getArgument(0);
            ctx.addSuggestion(new Suggestion(0, 8, "Original", null, "Reason", Severity.MEDIUM, "test", "TEST_01"));
            ctx.markStepApplied("PatternMatcher");
            return null;
        }).when(patternMatcher).execute(any(PipelineContext.class));

        NeutralizationResult result = deepMode.analyze(req);

        // Verify that DeepMode called all steps
        verify(fastMode).buildContext(req);
        verify(fastMode).executeSteps(context);
        verify(posTagging).execute(context);
        verify(patternMatcher).execute(context);

        // Assert results
        assertEquals("DEEP", result.modeUsed());
        assertEquals("Original text", result.originalText());
        assertNull(result.neutralizedText()); // No HIGH severity suggestions, so no rewriting
        assertEquals(1, result.suggestions().size());
        
        // Assert scores
        double expectedScore = 0.6 / 5.0; // 1 MEDIUM suggestion, maxScale = 5.0
        assertEquals(expectedScore, result.overallScore(), 0.001);
    }

    @Test
    void shouldAutoApplyHighSeveritySuggestions() {
        NeutralizationRequest req = new NeutralizationRequest("This is bad text", "EMAIL", "pt-BR", ModeType.DEEP);
        context = new PipelineContext("This is bad text", "EMAIL");
        when(fastMode.buildContext(any())).thenReturn(context);

        // Simulate adding a HIGH severity suggestion with a replacement
        doAnswer(invocation -> {
            PipelineContext ctx = invocation.getArgument(0);
            // Replace "bad" with "neutral"
            ctx.addSuggestion(new Suggestion(8, 11, "bad", "neutral", "Reason", Severity.HIGH, "test", "TEST_02"));
            return null;
        }).when(patternMatcher).execute(any(PipelineContext.class));

        NeutralizationResult result = deepMode.analyze(req);

        // The neutralized text MUST be present
        assertEquals("This is neutral text", result.neutralizedText());
        assertEquals(1, result.suggestions().size());
        
        // Assert scores (1 HIGH = 1.0 weight)
        assertEquals(1.0 / 5.0, result.overallScore(), 0.001);
    }

    @Test
    void shouldAutoApplyCriticalSeveritySuggestions() {
        NeutralizationRequest req = new NeutralizationRequest("This is terrible text", "EMAIL", "pt-BR", ModeType.DEEP);
        context = new PipelineContext("This is terrible text", "EMAIL");
        when(fastMode.buildContext(any())).thenReturn(context);

        // Simulate adding a CRITICAL severity suggestion with a replacement
        doAnswer(invocation -> {
            PipelineContext ctx = invocation.getArgument(0);
            // Replace "terrible" with "great"
            ctx.addSuggestion(new Suggestion(8, 16, "terrible", "great", "Reason", Severity.CRITICAL, "test", "TEST_03"));
            return null;
        }).when(patternMatcher).execute(any(PipelineContext.class));

        NeutralizationResult result = deepMode.analyze(req);

        // The neutralized text MUST be present
        assertEquals("This is great text", result.neutralizedText());
        assertEquals(1, result.suggestions().size());
        
        // Assert scores (1 CRITICAL = 1.5 weight)
        assertEquals(1.5 / 5.0, result.overallScore(), 0.001);
    }
}
