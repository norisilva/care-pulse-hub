package com.custodyreport.backend.repository;

import com.custodyreport.backend.domain.NotificationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationGroupRepository extends JpaRepository<NotificationGroup, Long> {
}
