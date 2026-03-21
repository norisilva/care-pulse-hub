package com.custodyreport.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import com.custodyreport.backend.engine.EngineRegistry;
import com.custodyreport.backend.engine.GenericEngine;
import com.custodyreport.backend.engine.training.TrainingData;
import com.custodyreport.backend.engine.training.TrainingDataRepository;
import com.custodyreport.backend.engine.training.TrainingPipeline;

public class FeedbackControllerTest {

    private TrainingDataRepository repository;
    private TrainingPipeline mockPipeline;
    private EngineRegistry engineRegistry;
    private FeedbackController controller;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(TrainingDataRepository.class);
        mockPipeline = Mockito.mock(TrainingPipeline.class);
        
        com.custodyreport.backend.engine.entity.EngineRepository engineRepository = Mockito.mock(com.custodyreport.backend.engine.entity.EngineRepository.class);
        com.custodyreport.backend.engine.strategy.StrategyFactory strategyFactory = Mockito.mock(com.custodyreport.backend.engine.strategy.StrategyFactory.class);
        com.custodyreport.backend.engine.distribution.DistributionEngine distributionEngine = Mockito.mock(com.custodyreport.backend.engine.distribution.DistributionEngine.class);
        
        engineRegistry = new EngineRegistry(engineRepository, strategyFactory, distributionEngine);
        
        // Register a real engine so feedback validation passes
        engineRegistry.register(new GenericEngine("engine1", "Engine 1", distributionEngine));
        
        controller = new FeedbackController(repository, List.of(mockPipeline), engineRegistry);
        
        when(mockPipeline.supports("python-sentiment")).thenReturn(true);
        when(mockPipeline.supports("unknown")).thenReturn(false);
    }

    @Test
    void testSubmitFeedback() {
        FeedbackController.FeedbackRequest req = new FeedbackController.FeedbackRequest();
        req.setStrategyId("python-sentiment");
        req.setInputJson("{\"text\":\"test\"}");
        req.setExpectedOutputJson("{\"sentiment\":\"neutral\"}");
        req.setAccepted(true);

        TrainingData savedData = TrainingData.builder().id(1L).engineId("engine1").strategyId("python-sentiment").build();
        when(repository.save(any(TrainingData.class))).thenReturn(savedData);

        ResponseEntity<?> response = controller.submitFeedback("engine1", req);
        
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testSubmitFeedbackWithInvalidEngine() {
        FeedbackController.FeedbackRequest req = new FeedbackController.FeedbackRequest();
        req.setStrategyId("python-sentiment");

        ResponseEntity<?> response = controller.submitFeedback("non-existent-engine", req);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testTriggerTrainingWithNoData() {
        when(repository.findByEngineIdAndStrategyId("engine1", "python-sentiment")).thenReturn(List.of());

        ResponseEntity<?> response = controller.triggerTraining("engine1", "python-sentiment");
        assertEquals(400, response.getStatusCode().value());
        assertEquals("No training data available for this strategy", response.getBody());
    }

    @Test
    void testTriggerTrainingSuccess() {
        List<TrainingData> dataList = List.of(new TrainingData());
        when(repository.findByEngineIdAndStrategyId("engine1", "python-sentiment")).thenReturn(dataList);
        
        TrainingPipeline.TrainingResult result = TrainingPipeline.TrainingResult.builder().status("SUCCESS").build();
        when(mockPipeline.train("engine1", "python-sentiment", dataList)).thenReturn(result);

        ResponseEntity<?> response = controller.triggerTraining("engine1", "python-sentiment");
        assertEquals(200, response.getStatusCode().value());
        assertEquals(result, response.getBody());
    }
}
