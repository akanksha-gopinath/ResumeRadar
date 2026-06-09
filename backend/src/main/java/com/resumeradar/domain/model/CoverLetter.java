package com.resumeradar.domain.model;

import java.time.Instant;

public record CoverLetter(
    String content,
    Instant generatedAt
) {}
