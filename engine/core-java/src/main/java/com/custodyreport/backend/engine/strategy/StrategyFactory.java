package com.custodyreport.backend.engine.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.custodyreport.backend.engine.entity.StrategyDefEntity;
import com.custodyreport.backend.engine.entity.StrategyDefRepository;
import com.custodyreport.backend.engine.strategy.python.PythonStrategyProxy;
import com.custodyreport.backend.engine.strategy.table.DecisionTableRule;
import com.custodyreport.backend.engine.strategy.table.DecisionTableStrategy;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StrategyFactory {

    private final StrategyDefRepository repository;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Strategy<?, ?>> staticStrategies = new ConcurrentHashMap<>();
    private ManagedChannel grpcChannel;

    @Value("${python.grpc.host:localhost}")
    private String grpcHost;

    @Value("${python.grpc.port:50051}")
    private int grpcPort;

    public StrategyFactory(StrategyDefRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        this.grpcChannel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
                .usePlaintext()
                // Round robin for basic resilience (bonus fix for gRPC SPOF)
                .defaultLoadBalancingPolicy("round_robin")
                .build();
    }

    public void registerStaticStrategy(Strategy<?, ?> strategy) {
        staticStrategies.put(strategy.getId(), strategy);
    }

    public Strategy<?, ?> resolveStrategy(String strategyId) {
        // 1. Check in-memory static strategies (e.g., Java algorithms)
        if (staticStrategies.containsKey(strategyId)) {
            return staticStrategies.get(strategyId);
        }

        // 2. Load from DB
        return repository.findById(strategyId)
                .map(this::hydrate)
                .orElse(null);
    }

    private Strategy<?, ?> hydrate(StrategyDefEntity entity) {
        StrategyMetadata metadata = new StrategyMetadata(entity.getVersion(), entity.getAuthor(), entity.getDescription(), null);
        
        switch (entity.getType()) {
            case DECISION_TABLE:
                try {
                    List<DecisionTableRule> rules = mapper.readValue(entity.getConfigJson(), new TypeReference<>() {});
                    return new DecisionTableStrategy(entity.getId(), entity.getName(), rules, metadata);
                } catch (JsonProcessingException e) {
                    log.error("Failed to parse Decision Table rules for strategy {}", entity.getId(), e);
                    throw new RuntimeException("Invalid strategy config", e);
                }
            case PYTHON_ALGORITHM:
                return new PythonStrategyProxy(entity.getId(), entity.getName(), grpcChannel);
            case JAVA_ALGORITHM:
            case COMPOSITE:
            default:
                log.warn("Cannot dynamically hydrate strategy of type {}", entity.getType());
                return null;
        }
    }
}
