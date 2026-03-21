package com.custodyreport.backend.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.custodyreport.backend.engine.entity.EngineEntity;
import com.custodyreport.backend.engine.entity.EngineRepository;
import com.custodyreport.backend.engine.strategy.Strategy;
import com.custodyreport.backend.engine.strategy.StrategyFactory;
import com.custodyreport.backend.engine.distribution.DistributionEngine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Registry Pattern for managing multiple AI Engines in the application.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EngineRegistry {
    
    private final Map<String, Engine> engines = new ConcurrentHashMap<>();
    private final EngineRepository engineRepository;
    private final StrategyFactory strategyFactory;
    private final DistributionEngine distributionEngine;
    
    @EventListener(ApplicationReadyEvent.class)
    public void loadEnginesFromDatabase() {
        log.info("Curing Systemic Amnesia: Loading engines from database...");
        List<EngineEntity> storedEngines = engineRepository.findAll();
        for (EngineEntity entity : storedEngines) {
            GenericEngine engine = new GenericEngine(entity.getId(), entity.getName(), distributionEngine);
            
            for (String strategyId : entity.getStrategyIds()) {
                Strategy<?, ?> strategy = strategyFactory.resolveStrategy(strategyId);
                if (strategy != null) {
                    engine.registerStrategy(strategy);
                } else {
                    log.error("Could not mount strategy {} for engine {}", strategyId, entity.getId());
                }
            }
            
            engines.put(engine.getId(), engine);
            log.info("Successfully loaded Engine: {} with {} strategies", engine.getId(), engine.getStrategies().size());
        }
    }

    public void register(Engine engine) {
        engines.put(engine.getId(), engine);
    }
    
    public Engine createAndPersistEngine(String id, String name) {
        EngineEntity entity = EngineEntity.builder().id(id).name(name).build();
        engineRepository.save(entity);
        GenericEngine engine = new GenericEngine(id, name, distributionEngine);
        engines.put(id, engine);
        return engine;
    }

    public Engine get(String engineId) {
        return engines.get(engineId);
    }
    
    public List<Engine> listAll() {
        return new ArrayList<>(engines.values());
    }
    
    public boolean contains(String engineId) {
        return engines.containsKey(engineId);
    }
    
    public void linkStrategyToEngine(String engineId, Strategy<?, ?> strategy) {
        Engine engine = get(engineId);
        if (engine != null) {
            engine.registerStrategy(strategy);
            
            engineRepository.findById(engineId).ifPresent(entity -> {
                entity.getStrategyIds().add(strategy.getId());
                engineRepository.save(entity);
            });
        }
    }

    /**
     * Shares a strategy across multiple engines if they are registered.
     */
    public void shareStrategy(Strategy<?,?> strategy, String... engineIds) {
        for (String id : engineIds) {
            linkStrategyToEngine(id, strategy);
        }
    }
}
