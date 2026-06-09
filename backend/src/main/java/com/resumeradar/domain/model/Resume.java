package com.resumeradar.domain.model;

import java.time.Instant;
import java.util.List;

public record Resume(
    Long id,
    String fileName,
    String parsedText,
    List<String> skills,
    Instant uploadedAt
) {}
