package com.resumeradar.domain.model;

import java.util.List;

public record JobSearchCriteria(
    String title,
    String location,
    List<WorkMode> workModes
) {}
