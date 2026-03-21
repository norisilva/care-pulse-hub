package com.custodyreport.backend.engine.distribution;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Defines when and how much a strategy should be used.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributionRule {
    
    private String strategyId;
    
    // Percentage from 0.0 to 1.0
    private BigDecimal percentage;
    
    // Null means no lower bound
    private LocalDateTime activeFrom;
    
    // Null means no upper bound
    private LocalDateTime activeTo;
    
    // Additional parameters for strategy configuration
    private Map<String, String> params;
    
    public boolean isActive(LocalDateTime date) {
        if (activeFrom != null && date.isBefore(activeFrom)) {
            return false;
        }
        if (activeTo != null && date.isAfter(activeTo)) {
            return false;
        }
        return true;
    }
}
