package com.custodyreport.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports", indexes = @Index(name = "idx_report_created_at", columnList = "createdAt DESC"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String assistedName;
    
    @Column(columnDefinition = "TEXT")
    private String field1;
    
    @Column(columnDefinition = "TEXT")
    private String field2;
    
    @Column(columnDefinition = "TEXT")
    private String field3;
    
    @Column(columnDefinition = "TEXT")
    private String field4;
    
    @Transient
    private boolean forceSend;

    private Long notificationGroupId;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
