package com.custodyreport.backend.engine.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "engines")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EngineEntity {
    
    @Id
    private String id;
    
    private String name;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private Set<String> strategyIds = new HashSet<>();
}
