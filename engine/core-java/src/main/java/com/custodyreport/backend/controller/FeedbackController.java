package com.custodyreport.backend.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.custodyreport.backend.engine.training.TrainingData;
import com.custodyreport.backend.engine.training.TrainingDataRepository;
import com.custodyreport.backend.engine.training.TrainingPipeline;
import com.custodyreport.backend.engine.EngineRegistry;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/engines/{engineId}/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final TrainingDataRepository repository;
    private final List<TrainingPipeline> pipelines;
    private final EngineRegistry engineRegistry;

    @PostMapping
    public ResponseEntity<?> submitFeedback(
            @PathVariable String engineId, 
            @RequestBody FeedbackRequest request) {
        
        if (!engineRegistry.contains(engineId)) {
            return ResponseEntity.badRequest().body("Engine not found: " + engineId);
        }
            
        TrainingData data = TrainingData.builder()
            .engineId(engineId)
            .strategyId(request.getStrategyId())
            .inputJson(request.getInputJson())
            .expectedOutputJson(request.getExpectedOutputJson())
            .actualOutputJson(request.getActualOutputJson())
            .accepted(request.isAccepted())
            .build();
            
        return ResponseEntity.ok(repository.save(data));
    }

    @PostMapping("/train")
    public ResponseEntity<?> triggerTraining(
            @PathVariable String engineId, 
            @RequestParam String strategyId) {
            
        List<TrainingData> historicalData = repository.findByEngineIdAndStrategyId(engineId, strategyId);
        if (historicalData.isEmpty()) {
            return ResponseEntity.badRequest().body("No training data available for this strategy");
        }
        
        // Find suitable pipeline
        for (TrainingPipeline pipeline : pipelines) {
            if (pipeline.supports(strategyId)) {
                return ResponseEntity.ok(pipeline.train(engineId, strategyId, historicalData));
            }
        }
        
        return ResponseEntity.badRequest().body("No training pipeline supports strategy: " + strategyId);
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> getMetrics(@PathVariable String engineId, @RequestParam String strategyId) {
         for (TrainingPipeline pipeline : pipelines) {
            if (pipeline.supports(strategyId)) {
                return ResponseEntity.ok(pipeline.getMetrics(strategyId));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @Data
    public static class FeedbackRequest {
        private String strategyId;
        private String inputJson;
        private String expectedOutputJson;
        private String actualOutputJson;
        private boolean accepted;
    }
}
