package com.custodyreport.backend.neutralization.steps;

import com.custodyreport.backend.neutralization.Severity;
import com.custodyreport.backend.neutralization.Suggestion;
import com.custodyreport.backend.neutralization.dictionary.DictionaryEntry;
import com.custodyreport.backend.neutralization.dictionary.SentimentDictionary;
import com.custodyreport.backend.neutralization.pipeline.PipelineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryMatcherStepTest {

    @Mock
    private SentimentDictionary dictionary;

    @InjectMocks
    private DictionaryMatcherStep step;

    @BeforeEach
    void setUp() throws IOException {
        // Mock stemming to just return the lowercased input (simplified)
        lenient().when(dictionary.stemToken(anyString())).thenAnswer(invocation -> invocation.getArgument(0).toString().toLowerCase());
    }

    @Test
    void shouldMatchSingleWordStem() {
        PipelineContext ctx = new PipelineContext("a pessoa ignorou a mensagem", "EMAIL");
        ctx.setSentences(List.of("a pessoa ignorou a mensagem"));

        DictionaryEntry mockEntry = new DictionaryEntry(
            "TEST_01", List.of("ignoro"), List.of(), List.of("não respondeu"), "Reason", null, null, Severity.MEDIUM, null
        );

        lenient().when(dictionary.findByStem("ignorou")).thenReturn(Optional.of(mockEntry));

        step.execute(ctx);

        List<Suggestion> suggestions = ctx.getSuggestions();
        assertEquals(1, suggestions.size());
        Suggestion s = suggestions.get(0);
        assertEquals("ignorou", s.originalSpan());
        assertEquals("não respondeu", s.suggestedReplacement());
        assertEquals(Severity.MEDIUM, s.severity());
        assertEquals("TEST_01", s.ruleId());
    }

    @Test
    void shouldMatchMultiWordStem() {
        PipelineContext ctx = new PipelineContext("eu me sinto traído por você", "EMAIL");
        ctx.setSentences(List.of("eu me sinto traído por você"));

        DictionaryEntry mockEntryMulti = new DictionaryEntry(
            "MULTI_01", List.of("me sinto traído"), List.of(), List.of("estou frustrado"), "Reason", null, null, Severity.HIGH, null
        );

        lenient().when(dictionary.getPhraseIndex()).thenReturn(Map.of("me sinto traído", mockEntryMulti));
        // Mock returning empty for single stems to ensure only phrase matches
        lenient().when(dictionary.findByStem(anyString())).thenReturn(Optional.empty());

        step.execute(ctx);

        List<Suggestion> suggestions = ctx.getSuggestions();
        assertEquals(1, suggestions.size());
        Suggestion s = suggestions.get(0);
        assertEquals("me sinto traído", s.originalSpan());
        assertEquals("estou frustrado", s.suggestedReplacement());
        assertEquals(Severity.HIGH, s.severity());
        assertEquals("MULTI_01", s.ruleId());
        assertEquals("dictionary_phrase", s.detectedBy());
    }

    @Test
    void shouldPrioritizeMultiWordOverSingleWordStems() {
        PipelineContext ctx = new PipelineContext("eu me sinto traído", "EMAIL");
        ctx.setSentences(List.of("eu me sinto traído"));

        DictionaryEntry mockEntryMulti = new DictionaryEntry(
            "MULTI_01", List.of("me sinto traído"), List.of(), List.of("estou frustrado"), "Reason", null, null, Severity.HIGH, null
        );
        DictionaryEntry mockEntrySingle = new DictionaryEntry(
            "SINGLE_01", List.of("traído"), List.of(), List.of("prejudicado"), "Reason", null, null, Severity.MEDIUM, null
        );

        lenient().when(dictionary.getPhraseIndex()).thenReturn(Map.of("me sinto traído", mockEntryMulti));
        lenient().when(dictionary.findByStem("traído")).thenReturn(Optional.of(mockEntrySingle));

        step.execute(ctx);

        List<Suggestion> suggestions = ctx.getSuggestions();
        assertEquals(1, suggestions.size()); // Should only match the phrase, single stem should be skipped
        Suggestion s = suggestions.get(0);
        assertEquals("me sinto traído", s.originalSpan());
        assertEquals("estou frustrado", s.suggestedReplacement());
        assertEquals("MULTI_01", s.ruleId());
    }
}
