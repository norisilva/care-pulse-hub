package com.custodyreport.backend.engine.strategy;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context that is passed to strategies during execution.
 * Allows strategies to share state or access global execution parameters.
 */
public class StrategyContext {
    private final String engineId;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public StrategyContext(String engineId) {
        this.engineId = engineId;
    }

    public String getEngineId() {
        return engineId;
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    public Map<String, Object> getAllAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
}
