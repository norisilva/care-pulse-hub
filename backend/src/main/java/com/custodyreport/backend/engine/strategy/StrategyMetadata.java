package com.custodyreport.backend.engine.strategy;

import java.util.Map;

public record StrategyMetadata(
    String version,
    String author,
    String description,
    Map<String, String> properties
) {}
