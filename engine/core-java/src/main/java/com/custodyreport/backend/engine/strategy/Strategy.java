package com.custodyreport.backend.engine.strategy;

/**
 * Interface that all AI and rule-based strategies must implement.
 * An Engine delegates evaluation to one or more Strategies.
 */
public interface Strategy<I, O> {
    
    String getId();
    
    String getName();
    
    StrategyType getType();
    
    StrategyMetadata getMetadata();
    
    O execute(I input, StrategyContext context);
}
