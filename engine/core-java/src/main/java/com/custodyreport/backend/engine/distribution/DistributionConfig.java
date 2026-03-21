package com.custodyreport.backend.engine.distribution;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistributionConfig {
    
    private String engineId;
    
    private List<DistributionRule> rules;
    
    // If strictMode is true, the active rules for any given date must sum exactly to 1.0 (100%)
    private boolean strictMode;

}
