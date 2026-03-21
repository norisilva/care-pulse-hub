package com.custodyreport.backend.service;

import com.custodyreport.backend.domain.SystemConfig;
import com.custodyreport.backend.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigService {

    private final SystemConfigRepository repository;
    private final com.custodyreport.backend.repository.NotificationGroupRepository groupRepository;

    public SystemConfig getConfig() {
        return repository.findById(1L).orElseGet(() -> {
            SystemConfig defaultConfig = new SystemConfig();
            defaultConfig.setId(1L);
            // Default master key to ensure it works out of the box if empty
            defaultConfig.setMasterKey("Xy7$vK9#mQ2!wP5*sL8@dR0^bT4&N1%j"); 
            return repository.save(defaultConfig);
        });
    }

    public SystemConfig updateConfig(SystemConfig newConfig) {
        newConfig.setId(1L); // Ensure singleton
        // The user edits plain values in UI, backend saves them encrypted
        return repository.save(newConfig);
    }
    
    public void saveConfigEncrypted(String plainMasterKey, String plainUser, String plainPass, String recipientEmail, String openaiKey, String userRole) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(plainMasterKey);
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        
        SystemConfig config = new SystemConfig();
        config.setId(1L);
        config.setMasterKey(plainMasterKey); // Store plain master key in DB
        config.setUserRole(userRole != null ? userRole : "1"); // Default to role 1 (Parents)
        config.setEmailUsername(plainUser != null ? encryptor.encrypt(plainUser) : null);
        config.setEmailPassword(plainPass != null ? encryptor.encrypt(plainPass) : null);
        config.setRecipientEmail(recipientEmail);
        config.setOpenAiApiKey(openaiKey != null ? encryptor.encrypt(openaiKey) : null);
        
        repository.save(config);

        // Sync Default Notification Group
        syncDefaultGroup(recipientEmail);
    }

    private void syncDefaultGroup(String recipientEmail) {
        if (recipientEmail == null || recipientEmail.isEmpty()) return;
        
        // Check if default group exists
        java.util.List<com.custodyreport.backend.domain.NotificationGroup> groups = groupRepository.findAll();
        com.custodyreport.backend.domain.NotificationGroup defaultGroup = groups.stream()
            .filter(g -> "Destinatário Padrão".equals(g.getName()))
            .findFirst()
            .orElseGet(() -> {
                com.custodyreport.backend.domain.NotificationGroup newG = new com.custodyreport.backend.domain.NotificationGroup();
                newG.setName("Destinatário Padrão");
                newG.setPeriodicity("ON-DEMAND");
                return newG;
            });
            
        defaultGroup.setRecipientEmail(recipientEmail);
        groupRepository.save(defaultGroup);
    }

    public String decrypt(String encryptedValue, String masterKey) {
        if (encryptedValue == null || encryptedValue.isEmpty()) return null;
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(masterKey);
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        return encryptor.decrypt(encryptedValue);
    }
}
