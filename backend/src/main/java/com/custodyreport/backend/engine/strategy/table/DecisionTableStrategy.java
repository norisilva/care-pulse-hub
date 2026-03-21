package com.custodyreport.backend.engine.strategy.table;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.custodyreport.backend.engine.strategy.Strategy;
import com.custodyreport.backend.engine.strategy.StrategyContext;
import com.custodyreport.backend.engine.strategy.StrategyMetadata;
import com.custodyreport.backend.engine.strategy.StrategyType;

public class DecisionTableStrategy implements Strategy<Map<String, Object>, DecisionResult> {

    private final String id;
    private final String name;
    private final List<DecisionTableRule> rules;
    private final StrategyMetadata metadata;

    public DecisionTableStrategy(String id, String name, List<DecisionTableRule> rules, StrategyMetadata metadata) {
        this.id = id;
        this.name = name;
        // Ensure rules are sorted by priority (higher priority first)
        if (rules != null) {
            this.rules = new java.util.ArrayList<>(rules);
            this.rules.sort(Comparator.comparingInt(DecisionTableRule::getPriority).reversed());
        } else {
            this.rules = new java.util.ArrayList<>();
        }
        this.metadata = metadata;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public StrategyType getType() {
        return StrategyType.DECISION_TABLE;
    }

    @Override
    public StrategyMetadata getMetadata() {
        return metadata;
    }

    @Override
    public DecisionResult execute(Map<String, Object> input, StrategyContext context) {
        if (rules == null || rules.isEmpty()) {
            return DecisionResult.builder().matched(false).build();
        }

        for (DecisionTableRule rule : rules) {
            if (matches(rule.getConditions(), input)) {
                return DecisionResult.builder()
                        .matched(true)
                        .matchedRuleId(rule.getRuleId())
                        .action(rule.getAction())
                        .outputData(rule.getOutputData())
                        .build();
            }
        }
        
        return DecisionResult.builder().matched(false).build();
    }

    private boolean matches(Map<String, Object> conditions, Map<String, Object> input) {
        if (conditions == null || conditions.isEmpty()) return true;
        if (input == null) return false;

        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String key = entry.getKey();
            Object expected = entry.getValue();
            Object actual = input.get(key);

            if (actual == null) {
                return false;
            }

            // Simple exact match logic (could be expanded to support > < regex etc)
            if (expected instanceof String expStr && actual instanceof Number actNum) {
                if (!evaluateCondition(expStr, actNum)) {
                    return false;
                }
            } else if (!expected.equals(actual)) {
                return false;
            }
        }

        return true;
    }
    
    // Supports ">10", "<5", etc.
    private boolean evaluateCondition(String expectedStr, Number actual) {
        if (expectedStr == null) return false;
        
        try {
            String trimmed = expectedStr.trim();
            if (trimmed.startsWith(">=")) {
                return actual.doubleValue() >= Double.parseDouble(trimmed.substring(2).trim());
            } else if (trimmed.startsWith("<=")) {
                return actual.doubleValue() <= Double.parseDouble(trimmed.substring(2).trim());
            } else if (trimmed.startsWith(">")) {
                return actual.doubleValue() > Double.parseDouble(trimmed.substring(1).trim());
            } else if (trimmed.startsWith("<")) {
                return actual.doubleValue() < Double.parseDouble(trimmed.substring(1).trim());
            } else if (trimmed.startsWith("=")) {
                return actual.doubleValue() == Double.parseDouble(trimmed.substring(1).trim());
            } else {
                return actual.doubleValue() == Double.parseDouble(trimmed);
            }
        } catch (NumberFormatException e) {
            return false; // Invalid format
        }
    }
}
