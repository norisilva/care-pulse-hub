package com.custodyreport.backend.engine.strategy.algorithm;

import java.util.function.BiFunction;

import com.custodyreport.backend.engine.strategy.Strategy;
import com.custodyreport.backend.engine.strategy.StrategyContext;
import com.custodyreport.backend.engine.strategy.StrategyMetadata;
import com.custodyreport.backend.engine.strategy.StrategyType;

/**
 * A wrapper that bridges any standard Java BiFunction<I, StrategyContext, O> into the Engine Strategy format.
 * The BiFunction receives both the input and the execution context.
 */
public class JavaAlgorithmStrategy<I, O> implements Strategy<I, O> {

    private final String id;
    private final String name;
    private final BiFunction<I, StrategyContext, O> algorithm;
    private final StrategyMetadata metadata;

    public JavaAlgorithmStrategy(String id, String name, BiFunction<I, StrategyContext, O> algorithm, StrategyMetadata metadata) {
        this.id = id;
        this.name = name;
        this.algorithm = algorithm;
        this.metadata = metadata;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public StrategyType getType() {
        return StrategyType.JAVA_ALGORITHM;
    }

    @Override
    public StrategyMetadata getMetadata() {
        return metadata;
    }

    @Override
    public O execute(I input, StrategyContext context) {
        return algorithm.apply(input, context);
    }
}
