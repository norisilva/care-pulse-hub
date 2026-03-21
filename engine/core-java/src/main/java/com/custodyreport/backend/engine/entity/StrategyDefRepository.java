package com.custodyreport.backend.engine.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategyDefRepository extends JpaRepository<StrategyDefEntity, String> {
}
