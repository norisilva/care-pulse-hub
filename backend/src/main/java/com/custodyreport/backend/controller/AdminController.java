package com.custodyreport.backend.controller;

import com.custodyreport.backend.repository.NotificationGroupRepository;
import com.custodyreport.backend.repository.ReportRepository;
import com.custodyreport.backend.repository.SuggestionRepository;
import com.custodyreport.backend.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = {"*", "null"})
public class AdminController {

    private final ReportRepository reportRepository;
    private final NotificationGroupRepository groupRepository;
    private final SystemConfigRepository configRepository;
    private final SuggestionRepository suggestionRepository;

    /**
     * Wipes ALL user data from the database.
     * Suggestions (static templates) are preserved.
     * Called only from the UI with a double-confirmation guard.
     */
    @DeleteMapping("/reset")
    public ResponseEntity<Void> resetDatabase() {
        reportRepository.deleteAll();
        groupRepository.deleteAll();
        configRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
