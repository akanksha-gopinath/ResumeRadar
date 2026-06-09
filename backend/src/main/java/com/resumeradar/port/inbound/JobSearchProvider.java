package com.resumeradar.port.inbound;

import com.resumeradar.domain.model.Job;
import com.resumeradar.domain.model.JobSearchCriteria;

import java.time.Duration;
import java.util.List;

public interface JobSearchProvider {

    String providerName();

    List<Job.Listing> search(JobSearchCriteria criteria);

    List<Job.Listing> searchRecent(JobSearchCriteria criteria, Duration lookback);
}
