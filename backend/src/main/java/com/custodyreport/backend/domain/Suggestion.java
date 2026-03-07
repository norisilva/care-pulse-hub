package com.custodyreport.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Suggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roleId; // Matches userRole: "1", "2", etc.
    private String scenario; // e.g., "Semana de Provas", "Férias", "Rotina Normal"

    @Column(length = 2000)
    private String field1Suggestion;
    @Column(length = 2000)
    private String field2Suggestion;
    @Column(length = 2000)
    private String field3Suggestion;
    @Column(length = 2000)
    private String field4Suggestion;
}
