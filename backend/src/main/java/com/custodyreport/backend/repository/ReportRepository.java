package com.custodyreport.backend.repository;

import com.custodyreport.backend.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    
    @Query("SELECT r FROM Report r ORDER BY r.createdAt DESC LIMIT 1")
    Optional<Report> findLatest();
}
