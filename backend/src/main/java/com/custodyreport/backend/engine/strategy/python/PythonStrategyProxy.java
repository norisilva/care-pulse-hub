package com.custodyreport.backend.engine.strategy.python;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.custodyreport.backend.engine.grpc.PythonStrategyGrpc;
import com.custodyreport.backend.engine.grpc.StrategyMetadataResponse;
import com.custodyreport.backend.engine.grpc.Empty;
import com.custodyreport.backend.engine.grpc.StrategyRequest;
import com.custodyreport.backend.engine.grpc.StrategyResponse;
import com.custodyreport.backend.engine.strategy.Strategy;
import com.custodyreport.backend.engine.strategy.StrategyContext;
import com.custodyreport.backend.engine.strategy.StrategyMetadata;
import com.custodyreport.backend.engine.strategy.StrategyType;
import io.grpc.ManagedChannel;
import java.util.HashMap;
import java.util.Map;

public class PythonStrategyProxy implements Strategy<Map<String, Object>, Map<String, Object>> {

    private final String id;
    private final String name;
    private final PythonStrategyGrpc.PythonStrategyBlockingStub stub;
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile StrategyMetadata metadataCache;

    public PythonStrategyProxy(String id, String name, ManagedChannel channel) {
        this.id = id;
        this.name = name;
        this.stub = PythonStrategyGrpc.newBlockingStub(channel);
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
        return StrategyType.PYTHON_ALGORITHM;
    }

    @Override
    public StrategyMetadata getMetadata() {
        if (metadataCache == null) {
            try {
                StrategyMetadataResponse res = stub.withDeadlineAfter(3, java.util.concurrent.TimeUnit.SECONDS)
                        .getMetadata(Empty.newBuilder().build());
                metadataCache = new StrategyMetadata(res.getVersion(), "Python RPC", res.getDescription(), null);
            } catch (Exception e) {
                // Return default if python service is down
                return new StrategyMetadata("unknown", "Python RPC", "RPC connection failed", null);
            }
        }
        return metadataCache;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input, StrategyContext context) {
        try {
            String jsonInput = mapper.writeValueAsString(input);
            
            // Map context attributes to string params
            Map<String, String> stringParams = new HashMap<>();
            context.getAllAttributes().forEach((k, v) -> stringParams.put(k, String.valueOf(v)));

            StrategyRequest request = StrategyRequest.newBuilder()
                    .setStrategyId(this.id)
                    .setInputJson(jsonInput)
                    .putAllParams(stringParams)
                    .build();

            StrategyResponse response = stub.withDeadlineAfter(5, java.util.concurrent.TimeUnit.SECONDS).execute(request);

            return mapper.readValue(response.getOutputJson(), new TypeReference<Map<String, Object>>() {});

        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize/deserialize payload for Python RPC or RPC timed out", e);
        }
    }
}
