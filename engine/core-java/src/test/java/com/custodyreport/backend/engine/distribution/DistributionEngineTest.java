package com.custodyreport.backend.engine.distribution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DistributionEngineTest {

    private final DistributionEngine engine = new DistributionEngine();

    @Test
    void testActiveRulesResolution() {
        DistributionRule pastRule = DistributionRule.builder()
                .strategyId("strat1")
                .percentage(new BigDecimal("0.5"))
                .activeFrom(LocalDateTime.of(2025, 1, 1, 0, 0))
                .activeTo(LocalDateTime.of(2025, 12, 31, 23, 59))
                .build();

        DistributionRule futureRule = DistributionRule.builder()
                .strategyId("strat2")
                .percentage(new BigDecimal("0.5"))
                .activeFrom(LocalDateTime.of(2027, 1, 1, 0, 0))
                .activeTo(LocalDateTime.of(2027, 12, 31, 23, 59))
                .build();

        DistributionRule activeRule = DistributionRule.builder()
                .strategyId("strat3")
                .percentage(BigDecimal.ONE)
                .activeFrom(null) // always active lower bound
                .activeTo(null)   // always active upper bound
                .build();

        LocalDateTime today = LocalDateTime.of(2026, 3, 20, 12, 0);

        List<DistributionRule> active = engine.resolveActiveRules(today, List.of(pastRule, futureRule, activeRule));
        assertEquals(1, active.size());
        assertEquals("strat3", active.get(0).getStrategyId());
    }

    @Test
    void testWeightedExecutionPlan() {
        DistributionRule r1 = DistributionRule.builder().strategyId("A").percentage(new BigDecimal("0.6")).build();
        DistributionRule r2 = DistributionRule.builder().strategyId("B").percentage(new BigDecimal("0.4")).build();

        Map<String, Double> plan = engine.buildWeightedExecution(List.of(r1, r2), true);
        assertEquals(0.6, plan.get("A"));
        assertEquals(0.4, plan.get("B"));
    }

    @Test
    void testStrictModeViolation() {
        DistributionRule r1 = DistributionRule.builder().strategyId("A").percentage(new BigDecimal("0.6")).build();
        DistributionRule r2 = DistributionRule.builder().strategyId("B").percentage(new BigDecimal("0.5")).build();

        assertThrows(IllegalStateException.class, () -> {
            engine.buildWeightedExecution(List.of(r1, r2), true);
        });
    }
}
