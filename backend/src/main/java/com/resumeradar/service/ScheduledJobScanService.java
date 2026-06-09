package com.resumeradar.service;

import com.resumeradar.adapter.provider.JobBoardProviderRegistry;
import com.resumeradar.domain.model.Job;
import com.resumeradar.domain.model.JobSearchCriteria;
import com.resumeradar.domain.model.WorkMode;
import com.resumeradar.entity.JobEntity;
import com.resumeradar.entity.SearchPreferencesEntity;
import com.resumeradar.entity.UserEntity;
import com.resumeradar.port.inbound.JobSearchProvider;
import com.resumeradar.port.outbound.JobRepository;
import com.resumeradar.port.outbound.SearchPreferencesRepository;
import com.resumeradar.port.outbound.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public final class ScheduledJobScanService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledJobScanService.class);

    private final JobBoardProviderRegistry registry;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final SearchPreferencesRepository preferencesRepository;
    private final MatchScoringService matchScoringService;

    public ScheduledJobScanService(JobBoardProviderRegistry registry,
                                    JobRepository jobRepository,
                                    UserRepository userRepository,
                                    SearchPreferencesRepository preferencesRepository,
                                    MatchScoringService matchScoringService) {
        this.registry = registry;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.preferencesRepository = preferencesRepository;
        this.matchScoringService = matchScoringService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void scanForNewPostings() {
        log.info("Starting scheduled job scan for new postings in last 24 hours");

        List<UserEntity> users = userRepository.findAll();
        for (UserEntity user : users) {
            try {
                scanForUser(user);
            } catch (Exception e) {
                log.error("Failed to scan jobs for user: {}", user.getDisplayName(), e);
            }
        }

        log.info("Scheduled job scan completed");
    }

    private void scanForUser(UserEntity user) {
        SearchPreferencesEntity prefs = preferencesRepository.findByUserId(user.getId())
            .orElse(null);

        if (prefs == null || prefs.getJobTitle() == null) {
            log.debug("No search preferences set for user {}, skipping scan", user.getDisplayName());
            return;
        }

        JobSearchCriteria criteria = new JobSearchCriteria(
            prefs.getJobTitle(), prefs.getLocation(), prefs.getActiveWorkModes());

        for (String providerName : registry.availableProviders()) {
            JobSearchProvider provider = registry.getSearchProvider(providerName);
            List<Job.Listing> newJobs = provider.searchRecent(criteria, Duration.ofHours(24));

            int savedCount = 0;
            for (Job.Listing job : newJobs) {
                if (!jobRepository.existsByExternalId(job.externalId())) {
                    JobEntity entity = toEntity(job);
                    JobEntity saved = jobRepository.save(entity);
                    savedCount++;

                    // Auto-score match for new jobs
                    try {
                        matchScoringService.scoreJob(user.getId(), saved.getId());
                    } catch (Exception e) {
                        log.warn("Failed to score match for job {}: {}", saved.getId(), e.getMessage());
                    }
                }
            }

            if (savedCount > 0) {
                log.info("Found {} new jobs from {} for user {}",
                    savedCount, providerName, user.getDisplayName());
            }
        }
    }

    private JobEntity toEntity(Job.Listing listing) {
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
        return entity;
    }
}
