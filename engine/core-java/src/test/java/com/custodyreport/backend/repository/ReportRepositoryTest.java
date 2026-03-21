package com.custodyreport.backend.repository;

import com.custodyreport.backend.domain.Report;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ReportRepositoryTest {

    @MockitoBean
    private org.springframework.mail.javamail.JavaMailSender javaMailSender;

    @Autowired
    private ReportRepository reportRepository;

    @Test
    void findLatest_shouldReturnMostRecentReport() throws InterruptedException {
        // Given
        Report olderReport = new Report();
        olderReport.setAssistedName("Test Person");
        olderReport.setField1("Health older");
        reportRepository.save(olderReport);
        
        // Wait a small amount so the timestamp is definitively later
        Thread.sleep(10); 

        Report newerReport = new Report();
        newerReport.setAssistedName("Test Person");
        newerReport.setField1("Health newer");
        reportRepository.save(newerReport);

        // When
        Optional<Report> latest = reportRepository.findLatest();

        // Then
        assertThat(latest).isPresent();
        assertThat(latest.get().getField1()).isEqualTo("Health newer");
    }

    @Test
    void findLatest_shouldReturnEmptyWhenNoReports() {
        Optional<Report> latest = reportRepository.findLatest();
        assertThat(latest).isEmpty();
    }
}
