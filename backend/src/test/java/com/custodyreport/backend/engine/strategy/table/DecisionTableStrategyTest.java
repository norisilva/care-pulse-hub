package com.custodyreport.backend.engine.strategy.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.custodyreport.backend.engine.strategy.StrategyContext;
import com.custodyreport.backend.engine.strategy.StrategyMetadata;

public class DecisionTableStrategyTest {

    @Test
    void testExactMatch() {
        DecisionTableRule rule = new DecisionTableRule(
            "rule1", 10, Map.of("category", "offensive"), "BLOCK", Map.of("reason", "Contains bad words")
        );
        DecisionTableStrategy strategy = new DecisionTableStrategy("dt1", "Test", List.of(rule), new StrategyMetadata("1.0", "Author", "Test", null));

        Map<String, Object> input = Map.of("category", "offensive", "score", 0.9);
        DecisionResult result = strategy.execute(input, new StrategyContext("engine1"));

        assertTrue(result.isMatched());
        assertEquals("BLOCK", result.getAction());
        assertEquals("rule1", result.getMatchedRuleId());
        
        // Mismatch case
        Map<String, Object> inputFail = Map.of("category", "neutral");
        assertFalse(strategy.execute(inputFail, new StrategyContext("engine1")).isMatched());
    }

    @Test
    void testNumericGreaterMatch() {
         DecisionTableRule rule = new DecisionTableRule(
            "rule2", 5, Map.of("score", ">0.8"), "WARN", null
        );
        DecisionTableStrategy strategy = new DecisionTableStrategy("dt1", "Test", List.of(rule), null);

        assertTrue(strategy.execute(Map.of("score", 0.9), new StrategyContext("engine1")).isMatched());
        assertTrue(strategy.execute(Map.of("score", 1.0), new StrategyContext("engine1")).isMatched());
        assertFalse(strategy.execute(Map.of("score", 0.8), new StrategyContext("engine1")).isMatched()); // >0.8 not >=
        assertFalse(strategy.execute(Map.of("score", 0.5), new StrategyContext("engine1")).isMatched());
    }
}
