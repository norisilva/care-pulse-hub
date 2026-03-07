package com.custodyreport.backend.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PolicyServiceTest {

    private final PolicyService policyService = new PolicyService();

    @Test
    void validateContent_shouldPassCleanContent() {
        String content = "A criança foi muito bem na escola hoje, comeu tudo.";
        assertDoesNotThrow(() -> policyService.validateContent(content));
    }

    @Test
    void validateContent_shouldThrowOnDirectProfanity() {
        String content = "Você é um idiota";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> policyService.validateContent(content));
        assertTrue(exception.getMessage().contains("idiota"));
    }

    @Test
    void validateContent_shouldThrowOnObfuscatedProfanity() {
        String content = "Voc3 é um 1di0t@";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> policyService.validateContent(content));
        assertTrue(exception.getMessage().contains("idiota"));
    }

    @Test
    void validateContent_shouldThrowOnSpacedProfanity() {
        String content = "i d i o t a";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> policyService.validateContent(content));
        assertTrue(exception.getMessage().contains("idiota"));
    }
    
    @Test
    void validateContent_shouldPassNull() {
        assertDoesNotThrow(() -> policyService.validateContent(null));
    }
}
