package com.custodyreport.backend.service;

import com.custodyreport.backend.domain.Report;
import com.custodyreport.backend.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Optional;

import com.custodyreport.backend.neutralization.NeutralizationFacade;
import com.custodyreport.backend.neutralization.ModeType;
import com.custodyreport.backend.neutralization.NeutralizationRequest;
import com.custodyreport.backend.neutralization.NeutralizationResult;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    
    private final ReportRepository reportRepository;
    private final PolicyService policyService;
    private final EmailService emailService;
    private final NeutralizationFacade neutralizationFacade;
    
    public Report saveReport(Report report) {
        // Validate offensive language (hard profanity filter)
        policyService.validateContent(report.getField1());
        policyService.validateContent(report.getField2());
        policyService.validateContent(report.getField3());
        policyService.validateContent(report.getField4());
        
        // AI Neutralization Backend Check (Blocks if has suggestions AND user didn't force send)
        if (!report.isForceSend()) {
            boolean hasViolations = checkAiViolations(report.getField1()) || 
                                    checkAiViolations(report.getField2()) || 
                                    checkAiViolations(report.getField3()) || 
                                    checkAiViolations(report.getField4());
                                    
            if (hasViolations) {
                throw new IllegalArgumentException("REJEITADO_PELA_IA: O relatório contém termos não neutros. Faça a revisão pelo frontend ou use a confirmação para enviar mesmo assim.");
            }
        }
        
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

    private boolean checkAiViolations(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        NeutralizationResult result = neutralizationFacade.analyze(
            new NeutralizationRequest(text, "custody", "pt-BR", ModeType.FAST)
        );
        return result.suggestions() != null && !result.suggestions().isEmpty();
    }
}
