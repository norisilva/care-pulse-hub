package com.custodyreport.backend.controller;

import com.custodyreport.backend.domain.NotificationGroup;
import com.custodyreport.backend.service.NotificationGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@CrossOrigin(origins = {"*", "null"})
public class NotificationGroupController {

    private final NotificationGroupService service;

    @GetMapping
    public ResponseEntity<List<NotificationGroup>> getAllGroups() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationGroup> getGroupById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<NotificationGroup> createOrUpdateGroup(@RequestBody NotificationGroup group) {
        return ResponseEntity.ok(service.save(group));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
