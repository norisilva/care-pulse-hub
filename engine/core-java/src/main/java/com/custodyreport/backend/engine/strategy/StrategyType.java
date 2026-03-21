package com.custodyreport.backend.engine.strategy;

public enum StrategyType {
    DECISION_TABLE,      // Baseada em tabelas de decisão (JSON/YAML)
    JAVA_ALGORITHM,      // Implementação Java pura
    PYTHON_ALGORITHM,    // Delegada a serviço Python via gRPC
    COMPOSITE            // Combinação de outras estratégias
}
