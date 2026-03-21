package com.custodyreport.backend.service;

import com.custodyreport.backend.domain.NotificationGroup;
import com.custodyreport.backend.repository.NotificationGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationGroupService {

    private final NotificationGroupRepository repository;

    public List<NotificationGroup> findAll() {
        return repository.findAll();
    }

    public Optional<NotificationGroup> findById(Long id) {
        return repository.findById(id);
    }

    public NotificationGroup save(NotificationGroup group) {
        return repository.save(group);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
