package com.resumeradar.service;

import com.resumeradar.adapter.provider.JobBoardProviderRegistry;
import com.resumeradar.domain.model.Job;
import com.resumeradar.domain.model.JobSearchCriteria;
import com.resumeradar.entity.JobEntity;
import com.resumeradar.port.inbound.JobSearchProvider;
import com.resumeradar.port.outbound.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public final class JobSearchService {

    private final JobBoardProviderRegistry registry;
    private final JobRepository jobRepository;

    public JobSearchService(JobBoardProviderRegistry registry,
                            JobRepository jobRepository) {
        this.registry = registry;
        this.jobRepository = jobRepository;
    }

    @Transactional
    public List<Job.Listing> searchJobs(JobSearchCriteria criteria) {
        JobSearchProvider provider = registry.getSearchProvider("linkedin");
        List<Job.Listing> results = provider.search(criteria);

        results.forEach(this::persistIfNew);
        return results;
    }

    @Transactional
    public List<Job.Listing> searchRecentJobs(JobSearchCriteria criteria) {
        JobSearchProvider provider = registry.getSearchProvider("linkedin");
        List<Job.Listing> results = provider.searchRecent(criteria, Duration.ofHours(24));

        results.forEach(this::persistIfNew);
        return results;
    }

    public List<JobEntity> getRecentlyDiscoveredJobs() {
        Instant since = Instant.now().minus(Duration.ofHours(24));
        return jobRepository.findRecentJobs(since);
    }

    private void persistIfNew(Job.Listing listing) {
        if (!jobRepository.existsByExternalId(listing.externalId())) {
            JobEntity entity = new JobEntity();
            entity.setExternalId(listing.externalId());
            entity.setTitle(listing.title());
            entity.setCompany(listing.company());
            entity.setLocation(listing.location());
            entity.setWorkMode(listing.workMode());
            entity.setDescription(listing.description());
            entity.setPlatform(listing.platform());
            entity.setApplicationMode(listing.applicationMode());
            entity.setApplyUrl(listing.applyUrl());
            entity.setPostedAt(listing.postedAt());
            entity.setDiscoveredAt(Instant.now());
            jobRepository.save(entity);
        }
    }
}
