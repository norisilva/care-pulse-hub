package com.custodyreport.backend.neutralization.pipeline;

@FunctionalInterface
public interface PipelineStep {
    void execute(PipelineContext context);

    default boolean shouldExecute(PipelineContext context) {
        return true;
    }
}
