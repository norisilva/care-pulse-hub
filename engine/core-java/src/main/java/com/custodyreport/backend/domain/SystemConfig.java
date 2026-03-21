package com.custodyreport.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "system_config")
@Data
public class SystemConfig {

    @Id
    private Long id = 1L; 

    private String masterKey;
    private String emailUsername;
    private String emailPassword;
    private String recipientEmail;
    
    // Future MVP integration
    private String openAiApiKey;
    
    // Multi-Role Support (V2)
    private String userRole;
}
