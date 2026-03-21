package com.custodyreport.backend.engine.strategy.table;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DecisionResult {
    private boolean matched;
    private String matchedRuleId;
    private String action;
    private Map<String, Object> outputData;
}
