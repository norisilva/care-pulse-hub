package com.custodyreport.backend.controller;

import com.custodyreport.backend.domain.SystemConfig;
import com.custodyreport.backend.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@CrossOrigin(origins = {"*", "null"})
public class ConfigController {

    private final ConfigService configService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getConfig() {
        SystemConfig config = configService.getConfig();
        boolean setupRequired = config.getEmailUsername() == null || config.getEmailPassword() == null || config.getRecipientEmail() == null;
        
        return ResponseEntity.ok(Map.of(
            "masterKey", config.getMasterKey(),
            "recipientEmail", config.getRecipientEmail() != null ? config.getRecipientEmail() : "",
            "setupRequired", setupRequired,
            "userRole", config.getUserRole() != null ? config.getUserRole() : "1"
        ));
    }

    @PostMapping
    public ResponseEntity<Void> updateConfig(@RequestBody Map<String, String> payload) {
        String masterKey = payload.get("masterKey");
        String plainUser = payload.get("emailUser");
        String plainPass = payload.get("emailPass");
        String recipientEmail = payload.get("recipientEmail");
        String openaiKey = payload.get("openaiKey");
        String userRole = payload.get("userRole");
        
        configService.saveConfigEncrypted(masterKey, plainUser, plainPass, recipientEmail, openaiKey, userRole);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/encrypt")
    public ResponseEntity<Map<String, String>> encryptValue(@RequestBody Map<String, String> payload) {
        String masterKey = payload.get("masterKey");
        String valueToEncrypt = payload.get("value");
        if (masterKey == null || valueToEncrypt == null) {
            return ResponseEntity.badRequest().build();
        }
        
        org.jasypt.encryption.pbe.StandardPBEStringEncryptor encryptor = new org.jasypt.encryption.pbe.StandardPBEStringEncryptor();
        encryptor.setPassword(masterKey);
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        String encrypted = encryptor.encrypt(valueToEncrypt);
        
        return ResponseEntity.ok(Map.of(
            "original", valueToEncrypt,
            "encrypted", "ENC(" + encrypted + ")"
        ));
    }
}
