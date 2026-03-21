package com.custodyreport.backend.engine.training;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "training_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingData {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String engineId;
    
    @Column(nullable = false)
    private String strategyId;
    
    @Column(length = 5000)
    private String inputJson;
    
    @Column(length = 5000)
    private String expectedOutputJson;
    
    @Column(length = 5000)
    private String actualOutputJson;
    
    private boolean accepted; // true if the user accepted the suggestion, false if they corrected it
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
