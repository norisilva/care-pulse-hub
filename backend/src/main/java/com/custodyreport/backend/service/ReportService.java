package com.custodyreport.backend.service;

import com.custodyreport.backend.domain.Report;
import com.custodyreport.backend.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    
    private final ReportRepository reportRepository;
    private final PolicyService policyService;
    private final EmailService emailService;
    
    public Report saveReport(Report report) {
        // Validate offensive language
        policyService.validateContent(report.getField1());
        policyService.validateContent(report.getField2());
        policyService.validateContent(report.getField3());
        policyService.validateContent(report.getField4());
        
        Report saved = reportRepository.save(report);
        
        try {
            emailService.sendStandardizedReport(saved);
        } catch (Exception e) {
            log.error("Error invoking email service for report ID: {}. Error: {}", saved.getId(), e.getMessage());
        }
        
        return saved;
    }

    
    public Optional<Report> getLastReport() {
        return reportRepository.findLatest();
    }
}
