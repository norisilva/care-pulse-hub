package com.custodyreport.backend.engine;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EngineRequest {
    private String engineId;
    private Object payload; // The main input (could be a String, a DTO, Map, etc)
    private Map<String, Object> parameters; // additional metadata/params
}
