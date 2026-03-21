package com.custodyreport.backend.neutralization.steps;

import com.custodyreport.backend.neutralization.availability.NlpModelLoader;
import com.custodyreport.backend.neutralization.pipeline.PipelineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PosTaggingStepTest {

    @Mock
    private NlpModelLoader modelLoader;

    @InjectMocks
    private PosTaggingStep step;

    private PipelineContext ctx;

    @BeforeEach
    void setUp() {
        ctx = new PipelineContext("Test sentence", "EMAIL");
        ctx.setSentences(List.of("Test sentence"));
    }

    @Test
    void shouldExecuteOnlyIfModelIsAvailable() {
        when(modelLoader.isAvailable()).thenReturn(true);
        assertTrue(step.shouldExecute(ctx));

        when(modelLoader.isAvailable()).thenReturn(false);
        assertFalse(step.shouldExecute(ctx));
    }

    @Test
    void executeShouldReturnEarlyIfModelNotAvailable() {
        when(modelLoader.isAvailable()).thenReturn(false);

        step.execute(ctx);

        // Verification: posTags list should remain empty, step should not be marked as applied
        assertTrue(ctx.getPosTags().isEmpty());
        assertTrue(ctx.getAppliedSteps().isEmpty());
    }
}
