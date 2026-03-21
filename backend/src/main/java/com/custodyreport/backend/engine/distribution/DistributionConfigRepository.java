package com.custodyreport.backend.engine.distribution;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * In-memory repository for managing engine distribution configurations.
 * Can be migrated to JPA/H2 if application restarts need to preserve configs.
 */
@Repository
public class DistributionConfigRepository {
    
    private final Map<String, DistributionConfig> store = new ConcurrentHashMap<>();

    public DistributionConfig save(DistributionConfig config) {
        store.put(config.getEngineId(), config);
        return config;
    }

    public Optional<DistributionConfig> findByEngineId(String engineId) {
        return Optional.ofNullable(store.get(engineId));
    }
    
    public void deleteByEngineId(String engineId) {
        store.remove(engineId);
    }
}
