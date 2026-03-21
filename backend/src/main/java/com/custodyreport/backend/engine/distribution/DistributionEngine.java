package com.custodyreport.backend.engine.distribution;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.custodyreport.backend.engine.strategy.Strategy;

@Service
public class DistributionEngine {

    /**
     * Filters rules that are globally active for the given time.
     */
    public List<DistributionRule> resolveActiveRules(LocalDateTime time, List<DistributionRule> rules) {
        if (rules == null || rules.isEmpty()) return List.of();
        
        return rules.stream()
                .filter(rule -> rule.isActive(time))
                .collect(Collectors.toList());
    }

    /**
     * Builds an execution plan with the strategy ID mapping to its percentage of execution.
     * Throws an exception in StrictMode if it doesn't add up to 100%.
     */
    public Map<String, Double> buildWeightedExecution(List<DistributionRule> activeRules, boolean strictMode) {
        Map<String, Double> plan = new HashMap<>();
        
        BigDecimal totalPercentage = BigDecimal.ZERO;
        for (DistributionRule rule : activeRules) {
            BigDecimal pct = rule.getPercentage() == null ? BigDecimal.ZERO : rule.getPercentage();
            plan.merge(rule.getStrategyId(), pct.doubleValue(), Double::sum); // We can output Double downstream but do BigDecimal summation
            totalPercentage = totalPercentage.add(pct);
        }
        
        if (strictMode && totalPercentage.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalStateException("Strict distribution mode is on and total percentage is not 100% (1.0). Total calculated: " + totalPercentage);
        }
        
        return plan;
    }
}
