package com.custodyreport.backend.neutralization.dictionary;

import java.util.List;

public record CnvVocabulary(
    List<String> feelingsWhenNeedsMet,
    List<String> feelingsWhenNeedsNotMet,
    List<String> universalNeeds
) {}
