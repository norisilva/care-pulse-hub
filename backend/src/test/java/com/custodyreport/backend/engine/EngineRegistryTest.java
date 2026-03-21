package com.custodyreport.backend.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.custodyreport.backend.engine.entity.EngineRepository;
import com.custodyreport.backend.engine.strategy.StrategyFactory;
import com.custodyreport.backend.engine.distribution.DistributionEngine;
import com.custodyreport.backend.engine.strategy.StrategyMetadata;
import com.custodyreport.backend.engine.strategy.algorithm.JavaAlgorithmStrategy;

public class EngineRegistryTest {

    private EngineRepository engineRepository;
    private StrategyFactory strategyFactory;
    private DistributionEngine distributionEngine;
    private EngineRegistry registry;

    @BeforeEach
    void setUp() {
        engineRepository = Mockito.mock(EngineRepository.class);
        strategyFactory = Mockito.mock(StrategyFactory.class);
        distributionEngine = Mockito.mock(DistributionEngine.class);
        registry = new EngineRegistry(engineRepository, strategyFactory, distributionEngine);
    }

    @Test
    void testEngineRegistrationAndDiscovery() {
        GenericEngine engineA = new GenericEngine("engine-a", "Engine A");
        GenericEngine engineB = new GenericEngine("engine-b", "Engine B");
        
        registry.register(engineA);
        registry.register(engineB);
        
        assertEquals(2, registry.listAll().size());
        assertNotNull(registry.get("engine-a"));
        assertNotNull(registry.get("engine-b"));
    }
    
    @Test
    void testStrategySharingAcrossEngines() {
        GenericEngine engineA = new GenericEngine("engine-a", "Engine A");
        GenericEngine engineB = new GenericEngine("engine-b", "Engine B");
        
        registry.register(engineA);
        registry.register(engineB);
        
        JavaAlgorithmStrategy<String, String> sharedStrategy = new JavaAlgorithmStrategy<>(
            "strat-shared", "Shared Strat", (input, ctx) -> input.toUpperCase(),
            new StrategyMetadata("1", "A", "desc", null));
            
        engineA.registerStrategy(sharedStrategy);
        assertEquals(1, engineA.getStrategies().size());
        assertEquals(0, engineB.getStrategies().size());
        
        registry.shareStrategy(sharedStrategy, "engine-b");
        
        assertEquals(1, engineA.getStrategies().size());
        assertEquals(1, engineB.getStrategies().size());
        assertTrue(engineB.getStrategies().stream().anyMatch(s -> s.getId().equals("strat-shared")));
    }

    @Test
    void testDuplicateEngineIdOverwrites() {
        GenericEngine original = new GenericEngine("same-id", "Original");
        GenericEngine replacement = new GenericEngine("same-id", "Replacement");
        
        registry.register(original);
        registry.register(replacement);
        
        assertEquals(1, registry.listAll().size());
        assertEquals("Replacement", registry.get("same-id").getName());
    }

    @Test
    void testGenericEngineProcess() {
        GenericEngine engine = new GenericEngine("test-engine", "Test Engine");

        JavaAlgorithmStrategy<Object, Object> strategy = new JavaAlgorithmStrategy<>(
            "echo", "Echo Strategy",
            (input, ctx) -> Map.of("echo", input.toString(), "engine", ctx.getEngineId()),
            null);

        engine.registerStrategy(strategy);

        EngineRequest request = EngineRequest.builder()
            .engineId("test-engine")
            .payload("hello")
            .build();

        EngineResult result = engine.process(request);

        assertTrue(result.isSuccessful());
        assertNotNull(result.getStrategyResults());
        assertTrue(result.getStrategyResults().containsKey("echo"));
    }
}
