package com.custodyreport.backend.neutralization.mode;

import com.custodyreport.backend.neutralization.NeutralizationRequest;
import com.custodyreport.backend.neutralization.NeutralizationResult;

public interface NeutralizationMode {
    NeutralizationResult analyze(NeutralizationRequest request);
}
