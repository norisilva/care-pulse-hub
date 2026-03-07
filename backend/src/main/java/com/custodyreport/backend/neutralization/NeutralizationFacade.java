package com.custodyreport.backend.neutralization;

import com.custodyreport.backend.neutralization.availability.HardwareProbe;
import com.custodyreport.backend.neutralization.mode.FastMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NeutralizationFacade {

    private final FastMode fastMode;
    // DeepMode and UltraMode will be injected here in later phases
    private final HardwareProbe hardwareProbe;

    public NeutralizationResult analyze(NeutralizationRequest request) {
        return switch (request.mode()) {
            case FAST -> fastMode.analyze(request);
            case DEEP, ULTRA -> fastMode.analyze(request); // Fallback to fast mode for now
        };
    }

    public CapabilitiesResponse detectCapabilities() {
        long availableRam = hardwareProbe.getAvailableRamMb();
        String activeProfile = "LITE";
        
        if (availableRam > 4096) {
            activeProfile = "ULTRA";
        } else if (availableRam > 2048) {
            activeProfile = "STANDARD";
        }

        return new CapabilitiesResponse(
            true,                              // fastMode sempre disponível
            false,                             // OpenNLP carregado? (Fase 2)
            false,                             // Semaphore Word2Vec (Fase 3a)
            false,                             // Ollama respondendo? (Fase 3b)
            availableRam,                      // RAM disponível
            activeProfile                      // LITE, STANDARD, ULTRA
        );
    }
}
