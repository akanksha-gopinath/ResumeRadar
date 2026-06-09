package com.resumeradar.domain.model;

import java.time.Instant;

public sealed interface Job permits Job.Listing, Job.Applied, Job.Rejected {

    record Listing(
        String externalId,
        String title,
        String company,
        String location,
        WorkMode workMode,
        String description,
        String platform,
        ApplicationMode applicationMode,
        Instant postedAt,
        String applyUrl
    ) implements Job {}

    record Applied(
        Long id,
        Listing listing,
        String coverLetterContent,
        Instant appliedAt,
        ApplicationMode mode
    ) implements Job {}

    record Rejected(
        Long id,
        Listing listing,
        String reason,
        Instant rejectedAt
    ) implements Job {}
}
