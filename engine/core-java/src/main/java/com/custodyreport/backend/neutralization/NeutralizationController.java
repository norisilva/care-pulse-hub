package com.custodyreport.backend.neutralization;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/neutralization")
@RequiredArgsConstructor
@CrossOrigin(origins = {"*", "null"})
public class NeutralizationController {

    private final NeutralizationFacade facade;

    @PostMapping("/fast")
    public ResponseEntity<NeutralizationResult> analyzeFast(@RequestBody NeutralizationRequest request) {
        return ResponseEntity.ok(facade.analyze(request.withMode(ModeType.FAST)));
    }

    @PostMapping("/deep")
    public ResponseEntity<NeutralizationResult> analyzeDeep(@RequestBody NeutralizationRequest request) {
        return ResponseEntity.ok(facade.analyze(request.withMode(ModeType.DEEP)));
    }

    @GetMapping("/capabilities")
    public ResponseEntity<CapabilitiesResponse> capabilities() {
        return ResponseEntity.ok(facade.detectCapabilities());
    }
}
