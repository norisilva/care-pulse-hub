package com.custodyreport.backend.service;

import org.springframework.stereotype.Service;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PolicyService {
    
    // POC: Simple list of offensive words (extendable)
    private static final List<String> OFFENSIVE_WORDS = Arrays.asList(
        "palavrao1", "ofensa2", "idiota", "imbecil", "culpa sua", "negligente"
    );
    
    public void validateContent(String content) {
        if (content == null) return;
        
        // Normalize basic evasion techniques (e.g. "@" -> "a", "4" -> "a")
        String normalized = Normalizer.normalize(content, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replace("@", "a")
                .replace("4", "a")
                .replace("3", "e")
                .replace("1", "i")
                .replace("0", "o")
                .replaceAll("[^a-z]", ""); // Strip spaces to prevent "i d i o t a"
        
        for (String word : OFFENSIVE_WORDS) {
            String cleanWord = word.toLowerCase().replaceAll("[^a-z]", "");
            if (Pattern.compile(Pattern.quote(cleanWord)).matcher(normalized).find()) {
                throw new IllegalArgumentException("O texto contém linguagem ofensiva ou não permitida: " + word);
            }
        }
    }
}
