package com.custodyreport.backend.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.custodyreport.backend.engine.Engine;
import com.custodyreport.backend.engine.EngineRegistry;
import com.custodyreport.backend.engine.EngineRequest;
import com.custodyreport.backend.engine.GenericEngine;
import com.custodyreport.backend.engine.strategy.Strategy;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/engines")
@RequiredArgsConstructor
public class EngineController {

    private final EngineRegistry registry;

    @PostMapping
    public ResponseEntity<EngineDto> createEngine(@RequestBody CreateEngineRequest request) {
        if (registry.contains(request.getId())) {
            return ResponseEntity.badRequest().build();
        }
        
        Engine engine = registry.createAndPersistEngine(request.getId(), request.getName());
        return ResponseEntity.ok(toDto(engine));
    }

    @GetMapping
    public ResponseEntity<List<EngineDto>> listEngines() {
        List<EngineDto> dtos = registry.listAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EngineDto> getEngine(@PathVariable String id) {
        Engine engine = registry.get(id);
        if (engine == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toDto(engine));
    }

    @PostMapping("/{id}/strategies/{strategyId}/share/{targetEngineId}")
    public ResponseEntity<?> shareStrategy(
            @PathVariable String id, 
            @PathVariable String strategyId, 
            @PathVariable String targetEngineId) {
            
        Engine source = registry.get(id);
        
        if (source == null || !registry.contains(targetEngineId)) {
            return ResponseEntity.notFound().build();
        }
        
        Strategy<?, ?> strategy = source.getStrategies().stream()
                .filter(s -> s.getId().equals(strategyId))
                .findFirst()
                .orElse(null);
                
        if (strategy == null) {
            return ResponseEntity.notFound().build();
        }
        
        registry.linkStrategyToEngine(targetEngineId, strategy);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<?> process(@PathVariable String id, @RequestBody EngineRequest request) {
        Engine engine = registry.get(id);
        if (engine == null) return ResponseEntity.notFound().build();
        
        request.setEngineId(id);
        return ResponseEntity.ok(engine.process(request));
    }

    private EngineDto toDto(Engine engine) {
        EngineDto dto = new EngineDto();
        dto.setId(engine.getId());
        dto.setName(engine.getName());
        dto.setStrategyIds(engine.getStrategies().stream().map(Strategy::getId).collect(Collectors.toList()));
        return dto;
    }

    @Data
    public static class CreateEngineRequest {
        private String id;
        private String name;
    }

    @Data
    public static class EngineDto {
        private String id;
        private String name;
        private List<String> strategyIds;
    }
}
