package com.custodyreport.backend.engine;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.custodyreport.backend.engine.distribution.DistributionConfig;
import com.custodyreport.backend.engine.distribution.DistributionEngine;
import com.custodyreport.backend.engine.distribution.DistributionRule;
import com.custodyreport.backend.engine.strategy.Strategy;
import com.custodyreport.backend.engine.strategy.StrategyContext;
import lombok.extern.slf4j.Slf4j;

/**
 * A generic implementation of an Engine that executes its registered strategies.
 * When a DistributionConfig is set, it uses the DistributionEngine to resolve
 * which strategies should run and their weighted percentages.
 * Otherwise, it falls back to executing ALL registered strategies.
 */
@Slf4j
public class GenericEngine implements Engine {

    private final String id;
    private final String name;
    private final Map<String, Strategy<?, ?>> strategies = new ConcurrentHashMap<>();
    private final DistributionEngine distributionEngine;
    private DistributionConfig distributionConfig;

    public GenericEngine(String id, String name) {
        this(id, name, new DistributionEngine());
    }

    public GenericEngine(String id, String name, DistributionEngine distributionEngine) {
        this.id = id;
        this.name = name;
        this.distributionEngine = distributionEngine;
    }

    public void setDistributionConfig(DistributionConfig config) {
        this.distributionConfig = config;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    @Override
    public EngineResult process(EngineRequest request) {
        long startNanos = System.nanoTime();
        StrategyContext context = new StrategyContext(this.id);
        
        if (request.getParameters() != null) {
            request.getParameters().forEach(context::setAttribute);
        }

        Map<String, Object> strategyResults = new ConcurrentHashMap<>();
        
        try {
            // Determine which strategies to run
            Map<String, Double> executionPlan = resolveExecutionPlan();
            
            for (Map.Entry<String, Double> entry : executionPlan.entrySet()) {
                String strategyId = entry.getKey();
                Double weight = entry.getValue();
                
                Strategy<?, ?> strategy = strategies.get(strategyId);
                if (strategy == null) {
                    log.warn("Strategy {} configured in distribution but not registered in engine {}", strategyId, id);
                    continue;
                }
                
                // Add distribution weight to context so strategies can use it
                context.setAttribute("_distribution_weight", weight);
                
                Strategy<Object, Object> typedStrategy = (Strategy<Object, Object>) strategy;
                Object result = typedStrategy.execute(request.getPayload(), context);
                if (result != null) {
                    strategyResults.put(strategy.getId(), result);
                }
            }

            return EngineResult.builder()
                .engineId(this.id)
                .strategyResults(strategyResults)
                .finalOutput(strategyResults)
                .executionTimeMs((System.nanoTime() - startNanos) / 1_000_000.0)
                .successful(true)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to process request in GenericEngine: {}", id, e);
            return EngineResult.builder()
                .engineId(this.id)
                .successful(false)
                .errorMessage("Engine error: " + e.getMessage())
                .executionTimeMs((System.nanoTime() - startNanos) / 1_000_000.0)
                .build();
        }
    }

    /**
     * If a DistributionConfig is set, use the DistributionEngine to resolve
     * active rules and build a weighted plan. Otherwise, run all strategies at 100%.
     */
    private Map<String, Double> resolveExecutionPlan() {
        if (distributionConfig != null && distributionConfig.getRules() != null) {
            List<DistributionRule> activeRules = distributionEngine.resolveActiveRules(
                    java.time.LocalDateTime.now(), distributionConfig.getRules());
            
            if (!activeRules.isEmpty()) {
                return distributionEngine.buildWeightedExecution(
                        activeRules, distributionConfig.isStrictMode());
            }
        }
        
        // Fallback: run all strategies at 100%
        Map<String, Double> allStrategies = new ConcurrentHashMap<>();
        strategies.keySet().forEach(id -> allStrategies.put(id, 1.0));
        return allStrategies;
    }

    @Override
    public List<Strategy<?, ?>> getStrategies() {
        return new ArrayList<>(strategies.values());
    }

    @Override
    public void registerStrategy(Strategy<?, ?> strategy) {
        strategies.put(strategy.getId(), strategy);
    }

    @Override
    public void removeStrategy(String strategyId) {
        strategies.remove(strategyId);
    }
}
