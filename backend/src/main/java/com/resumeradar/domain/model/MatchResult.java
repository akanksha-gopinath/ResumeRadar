package com.resumeradar.domain.model;

import java.util.List;

public record MatchResult(
    int matchPercentage,
    List<String> matchedSkills,
    List<String> missingSkills,
    String summary
) {
    public static final int MINIMUM_MATCH_THRESHOLD = 80;

    public boolean isEligibleForApplication() {
        return matchPercentage >= MINIMUM_MATCH_THRESHOLD;
    }
}
