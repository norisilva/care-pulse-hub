package com.custodyreport.backend.controller;

import com.custodyreport.backend.domain.Report;
import com.custodyreport.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = {"*", "null"}) // Allow file:// execution (which sends 'Origin: null')
public class ReportController {
    
    private final ReportService reportService;
    
    @PostMapping
    public ResponseEntity<Report> createReport(@RequestBody Report report) {
        return ResponseEntity.ok(reportService.saveReport(report));
    }
    
    @GetMapping("/last")
    public ResponseEntity<Report> getLastReport() {
        return reportService.getLastReport()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
