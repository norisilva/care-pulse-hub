package com.custodyreport.backend.engine.training;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.custodyreport.backend.engine.grpc.PythonStrategyGrpc;
import com.custodyreport.backend.engine.grpc.TrainRequest;
import com.custodyreport.backend.engine.grpc.TrainResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PythonTrainingPipeline implements TrainingPipeline {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ManagedChannel grpcChannel;

    public PythonTrainingPipeline(
            @Value("${python.grpc.host:localhost}") String host,
            @Value("${python.grpc.port:50051}") int port) {
        this.grpcChannel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        log.info("PythonTrainingPipeline initialized with gRPC target {}:{}", host, port);
    }

    @Override
    public boolean supports(String strategyId) {
        return strategyId != null && strategyId.startsWith("python-");
    }

    @Override
    public TrainingResult train(String engineId, String strategyId, List<TrainingData> historicalData) {
        PythonStrategyGrpc.PythonStrategyBlockingStub stub = PythonStrategyGrpc.newBlockingStub(grpcChannel);
        
        try {
            String trainingDataJson = mapper.writeValueAsString(historicalData);
            
            TrainRequest request = TrainRequest.newBuilder()
                    .setStrategyId(strategyId)
                    .setTrainingDataJson(trainingDataJson)
                    .build();
                    
            TrainResponse response = stub.train(request);
            
            return TrainingResult.builder()
                    .jobId(UUID.randomUUID().toString())
                    .status(response.getStatus())
                    .message("New accuracy: " + response.getNewAccuracy())
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to call Python training pipeline for strategy {}", strategyId, e);
            return TrainingResult.builder()
                    .jobId(UUID.randomUUID().toString())
                    .status("FAILED")
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public TrainingStatus getStatus(String trainingJobId) {
        return TrainingStatus.builder()
                .jobId(trainingJobId)
                .state("COMPLETED")
                .progress(1.0)
                .build();
    }

    @Override
    public List<TrainingMetric> getMetrics(String strategyId) {
        return List.of(
            TrainingMetric.builder()
                .strategyId(strategyId)
                .accuracy(0.85)
                .dataPoints(100)
                .build()
        );
    }
}
