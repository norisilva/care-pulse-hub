package com.custodyreport.backend.neutralization;

import com.custodyreport.backend.engine.Engine;
import com.custodyreport.backend.engine.EngineRequest;
import com.custodyreport.backend.engine.EngineResult;
import com.custodyreport.backend.engine.strategy.Strategy;
import com.custodyreport.backend.neutralization.mode.DeepMode;
import com.custodyreport.backend.neutralization.mode.FastMode;
import com.custodyreport.backend.neutralization.mode.NeutralizationMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing the Engine interface for the current CarePulse implementation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CarePulseEngine implements Engine {

    public static final String ENGINE_ID = "carepulse";
    
    private final FastMode fastMode;
    private final DeepMode deepMode;
    
    private final Map<String, Strategy<?, ?>> strategies = new ConcurrentHashMap<>();

    @Override
    public String getId() {
        return ENGINE_ID;
    }

    @Override
    public String getName() {
        return "CarePulse Neutralization Engine";
    }

    @Override
    public EngineResult process(EngineRequest request) {
        long start = System.currentTimeMillis();
        
        try {
            // For now, extract the payload and pass to legacy NeuralizationFacade logic
            NeutralizationRequest nReq = null;
            if (request.getPayload() instanceof NeutralizationRequest) {
                nReq = (NeutralizationRequest) request.getPayload();
            } else if (request.getPayload() instanceof String) {
                // Determine mode from parameters (default to FAST if unspecified)
                ModeType targetMode = ModeType.FAST;
                if (request.getParameters() != null && "DEEP".equalsIgnoreCase((String) request.getParameters().get("mode"))) {
                    targetMode = ModeType.DEEP;
                }
                nReq = new NeutralizationRequest((String) request.getPayload(), "generic", "pt-BR", targetMode);
            } else {
                throw new IllegalArgumentException("Unsupported payload format for CarePulse Engine: " + request.getPayload().getClass());
            }

            NeutralizationMode modeExecutor = (nReq.mode() == ModeType.DEEP || nReq.mode() == ModeType.ULTRA) ? deepMode : fastMode;
            NeutralizationResult nResult = modeExecutor.analyze(nReq);
            
            return EngineResult.builder()
                .engineId(getId())
                .finalOutput(nResult)
                .executionTimeMs(System.currentTimeMillis() - start)
                .successful(true)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to process request in CarePulseEngine", e);
            return EngineResult.builder()
                .engineId(getId())
                .successful(false)
                .errorMessage("Error: " + e.getMessage())
                .executionTimeMs(System.currentTimeMillis() - start)
                .build();
        }
    }

    @Override
    public List<Strategy<?, ?>> getStrategies() {
        return new ArrayList<>(strategies.values());
    }

    @Override
    public void registerStrategy(Strategy<?, ?> strategy) {
        strategies.put(strategy.getId(), strategy);
    }

    @Override
    public void removeStrategy(String strategyId) {
        strategies.remove(strategyId);
    }
}
