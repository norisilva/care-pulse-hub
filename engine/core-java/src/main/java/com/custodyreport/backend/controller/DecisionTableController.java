package com.custodyreport.backend.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.custodyreport.backend.engine.Engine;
import com.custodyreport.backend.engine.EngineRegistry;
import com.custodyreport.backend.engine.entity.StrategyDefEntity;
import com.custodyreport.backend.engine.entity.StrategyDefRepository;
import com.custodyreport.backend.engine.strategy.StrategyMetadata;
import com.custodyreport.backend.engine.strategy.StrategyType;
import com.custodyreport.backend.engine.strategy.table.DecisionTableRule;
import com.custodyreport.backend.engine.strategy.table.DecisionTableStrategy;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/engines/{engineId}/strategies/decision-table")
@RequiredArgsConstructor
public class DecisionTableController {

    private final EngineRegistry registry;
    private final StrategyDefRepository strategyRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping
    public ResponseEntity<?> createStrategy(@PathVariable String engineId, @RequestBody CreateDecisionTableRequest request) {
        Engine engine = registry.get(engineId);
        if (engine == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            String configJson = mapper.writeValueAsString(request.getRules());
            
            StrategyDefEntity entity = StrategyDefEntity.builder()
                .id(request.getId())
                .name(request.getName())
                .type(StrategyType.DECISION_TABLE)
                .configJson(configJson)
                .description(request.getDescription())
                .version(request.getVersion())
                .author(request.getAuthor())
                .build();
                
            strategyRepository.save(entity);
            
            StrategyMetadata meta = new StrategyMetadata(request.getVersion(), request.getAuthor(), request.getDescription(), null);
            DecisionTableStrategy strategy = new DecisionTableStrategy(request.getId(), request.getName(), request.getRules(), meta);
            
            registry.linkStrategyToEngine(engineId, strategy);
            return ResponseEntity.ok(strategy);
            
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Invalid rules format");
        }
    }

    @DeleteMapping("/{strategyId}")
    public ResponseEntity<?> deleteStrategy(@PathVariable String engineId, @PathVariable String strategyId) {
        Engine engine = registry.get(engineId);
        if (engine == null) {
            return ResponseEntity.notFound().build();
        }

        engine.removeStrategy(strategyId);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class CreateDecisionTableRequest {
        private String id;
        private String name;
        private String description;
        private String version;
        private String author;
        private List<DecisionTableRule> rules;
    }
}
