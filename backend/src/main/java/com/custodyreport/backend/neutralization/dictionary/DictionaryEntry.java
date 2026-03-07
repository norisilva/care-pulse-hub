package com.custodyreport.backend.neutralization.dictionary;

import com.custodyreport.backend.neutralization.Severity;
import java.util.List;

public record DictionaryEntry(
    String ruleId,
    List<String> stems,
    List<String> original,
    List<String> suggestions,
    String reason,
    String pattern,
    String type,
    Severity severity
) {}
