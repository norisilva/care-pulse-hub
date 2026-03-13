package com.custodyreport.backend.neutralization;

import com.custodyreport.backend.neutralization.availability.HardwareProbe;
import com.custodyreport.backend.neutralization.availability.NlpModelLoader;
import com.custodyreport.backend.neutralization.mode.DeepMode;
import com.custodyreport.backend.neutralization.mode.FastMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NeutralizationFacade {

    private final FastMode fastMode;
    private final DeepMode deepMode;
    private final HardwareProbe hardwareProbe;
    private final NlpModelLoader nlpModelLoader;

    public NeutralizationResult analyze(NeutralizationRequest request) {
        return switch (request.mode()) {
            case FAST -> fastMode.analyze(request);
            case DEEP -> deepMode.analyze(request);
            case ULTRA -> deepMode.analyze(request); // Fase 3 — por agora delega para DeepMode
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
            nlpModelLoader.isAvailable(),      // OpenNLP POS model carregado?
            false,                             // Semaphore Word2Vec (Fase 3a)
            false,                             // Ollama respondendo? (Fase 3b)
            availableRam,                      // RAM disponível
            activeProfile                      // LITE, STANDARD, ULTRA
        );
    }
}

