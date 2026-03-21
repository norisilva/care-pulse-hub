package com.custodyreport.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private String periodicity; // DAILY, WEEKLY, ON-DEMAND

    // Comma-separated list of subjects, or just a simple string for now
    @Column(columnDefinition = "TEXT")
    private String subjects;
}
