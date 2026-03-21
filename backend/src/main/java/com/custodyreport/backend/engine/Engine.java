package com.custodyreport.backend.engine;

import com.custodyreport.backend.engine.strategy.Strategy;
import java.util.List;

/**
 * An Engine aggregates multiple Strategies and routes requests through them.
 */
public interface Engine {
    
    String getId();
    
    String getName();
    
    EngineResult process(EngineRequest request);
    
    List<Strategy<?, ?>> getStrategies();
    
    void registerStrategy(Strategy<?, ?> strategy);
    
    void removeStrategy(String strategyId);
}
