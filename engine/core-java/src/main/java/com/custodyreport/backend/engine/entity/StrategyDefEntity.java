package com.custodyreport.backend.engine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.custodyreport.backend.engine.strategy.StrategyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "strategy_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyDefEntity {
    
    @Id
    private String id;
    
    private String name;
    
    @Enumerated(EnumType.STRING)
    private StrategyType type;
    
    // JSON mapping of the configuration (e.g. DecisionTable rules)
    @Column(columnDefinition = "TEXT")
    private String configJson;
    
    private String description;
    
    private String version;
    
    private String author;
}
