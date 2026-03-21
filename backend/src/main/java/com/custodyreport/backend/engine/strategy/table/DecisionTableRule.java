package com.custodyreport.backend.engine.strategy.table;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionTableRule {
    private String ruleId;
    private int priority;
    
    // Key is the field name, Value is the expected string/number/boolean 
    // Example: {"category": "offensive", "confidence": ">0.8"}
    private Map<String, Object> conditions; 
    
    private String action; // e.g., "BLOCK", "WARN", "ALLOW"
    private Map<String, Object> outputData;
}
