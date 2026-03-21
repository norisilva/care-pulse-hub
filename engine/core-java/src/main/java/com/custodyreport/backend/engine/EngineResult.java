package com.custodyreport.backend.engine;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EngineResult {
    private String engineId;
    private Object finalOutput; // Processed output
    private Map<String, Object> strategyResults; // Results by strategy ID
    private double executionTimeMs;
    private boolean successful;
    private String errorMessage;
}
