package com.custodyreport.backend.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.custodyreport.backend.engine.distribution.DistributionConfig;
import com.custodyreport.backend.engine.distribution.DistributionConfigRepository;
import com.custodyreport.backend.engine.distribution.DistributionEngine;
import com.custodyreport.backend.engine.distribution.DistributionRule;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/engines/{engineId}/distribution")
@RequiredArgsConstructor
public class DistributionController {

    private final DistributionConfigRepository repository;
    private final DistributionEngine distributionEngine;

    @PostMapping
    public ResponseEntity<DistributionConfig> setConfiguration(
            @PathVariable String engineId, 
            @RequestBody DistributionConfigRequest request) {
            
        DistributionConfig config = DistributionConfig.builder()
            .engineId(engineId)
            .rules(request.getRules())
            .strictMode(request.isStrictMode())
            .build();
            
        return ResponseEntity.ok(repository.save(config));
    }

    @GetMapping
    public ResponseEntity<DistributionConfig> getConfiguration(@PathVariable String engineId) {
        return repository.findByEngineId(engineId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Double>> getActiveDistribution(
            @PathVariable String engineId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime time) {
            
        return repository.findByEngineId(engineId)
                .map(config -> {
                    java.time.LocalDateTime targetTime = time != null ? time : java.time.LocalDateTime.now();
                    List<DistributionRule> activeRules = distributionEngine.resolveActiveRules(targetTime, config.getRules());
                    Map<String, Double> plan = distributionEngine.buildWeightedExecution(activeRules, config.isStrictMode());
                    return ResponseEntity.ok(plan);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Data
    public static class DistributionConfigRequest {
        private boolean strictMode;
        private List<DistributionRule> rules;
    }
}
