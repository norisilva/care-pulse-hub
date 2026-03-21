package com.custodyreport.backend.engine.strategy.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.custodyreport.backend.engine.strategy.StrategyContext;

public class JavaAlgorithmStrategyTest {

    @Test
    void testAlgorithmExecution() {
        JavaAlgorithmStrategy<String, String> strategy = new JavaAlgorithmStrategy<>(
                "upper", "Uppercase Algorithm", (input, ctx) -> input.toUpperCase(), null);
                
        String result = strategy.execute("hello world", new StrategyContext("engine1"));
        assertEquals("HELLO WORLD", result);
    }

    @Test
    void testAlgorithmCanAccessContext() {
        JavaAlgorithmStrategy<String, String> strategy = new JavaAlgorithmStrategy<>(
                "ctx-test", "Context Test",
                (input, ctx) -> input + " [engine=" + ctx.getEngineId() + "]",
                null);

        StrategyContext ctx = new StrategyContext("my-engine");
        String result = strategy.execute("hello", ctx);
        assertEquals("hello [engine=my-engine]", result);
    }
}
