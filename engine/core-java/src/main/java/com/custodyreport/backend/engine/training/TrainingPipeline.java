package com.custodyreport.backend.engine.training;

import java.util.List;
import lombok.Builder;
import lombok.Data;

public interface TrainingPipeline {
    
    boolean supports(String strategyId);
    
    TrainingResult train(String engineId, String strategyId, List<TrainingData> historicalData);
    
    TrainingStatus getStatus(String trainingJobId);
    
    List<TrainingMetric> getMetrics(String strategyId);
    
    @Data
    @Builder
    class TrainingResult {
        private String jobId;
        private String status;
        private String message;
    }
    
    @Data
    @Builder
    class TrainingStatus {
        private String jobId;
        private String state; // PENDING, RUNNING, COMPLETED, FAILED
        private double progress;
    }
    
    @Data
    @Builder
    class TrainingMetric {
        private String strategyId;
        private double accuracy;
        private long dataPoints;
    }
}
