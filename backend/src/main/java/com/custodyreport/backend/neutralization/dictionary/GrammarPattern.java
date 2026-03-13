package com.custodyreport.backend.neutralization.dictionary;

import com.custodyreport.backend.neutralization.Severity;

import java.util.List;

/**
 * Representa um padrão gramatical baseado em sequência de POS tags.
 * Usado pelo PatternMatcherStep para detectar construções problemáticas.
 *
 * @param ruleId         identificador único do padrão (ex: "ACCUSATORY_PATTERN_001")
 * @param posSequence    sequência de tags esperada (ex: ["PRON", "ADV", "VERB"]) — imutável
 * @param description    descrição humana da razão da violação
 * @param severity       severidade da violação
 * @param suggestion     sugestão de reformulação para o usuário
 * @param requiredStems  se preenchido, pelo menos um stem deve estar presente no span
 *                       (evita falsos positivos em padrões genéricos como VERB+ADV)
 */
public record GrammarPattern(
    String ruleId,
    List<String> posSequence,
    String description,
    Severity severity,
    String suggestion,
    List<String> requiredStems
) {
    public GrammarPattern {
        // Garante imutabilidade — chamadores não podem mutar as listas após construção
        posSequence = List.copyOf(posSequence);
        requiredStems = requiredStems != null ? List.copyOf(requiredStems) : List.of();
    }
}

