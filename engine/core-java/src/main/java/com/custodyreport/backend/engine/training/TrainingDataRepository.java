package com.custodyreport.backend.engine.training;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingDataRepository extends JpaRepository<TrainingData, Long> {
    
    List<TrainingData> findByEngineIdAndStrategyId(String engineId, String strategyId);
    
    long countByStrategyIdAndAccepted(String strategyId, boolean accepted);
}
