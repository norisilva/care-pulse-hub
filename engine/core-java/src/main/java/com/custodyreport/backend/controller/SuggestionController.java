package com.custodyreport.backend.controller;

import com.custodyreport.backend.domain.Suggestion;
import com.custodyreport.backend.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
@CrossOrigin(origins = {"*", "null"})
public class SuggestionController {
    private final SuggestionService service;

    @GetMapping("/{roleId}")
    public ResponseEntity<List<Suggestion>> getSuggestions(@PathVariable String roleId) {
        return ResponseEntity.ok(service.getSuggestionsByRole(roleId));
    }
}
