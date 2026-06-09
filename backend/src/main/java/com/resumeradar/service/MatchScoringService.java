package com.resumeradar.service;

import com.resumeradar.domain.model.Job;
import com.resumeradar.domain.model.MatchResult;
import com.resumeradar.domain.model.Resume;
import com.resumeradar.entity.JobEntity;
import com.resumeradar.entity.JobMatchEntity;
import com.resumeradar.entity.UserEntity;
import com.resumeradar.port.inbound.ResumeMatchScorer;
import com.resumeradar.port.outbound.JobMatchRepository;
import com.resumeradar.port.outbound.JobRepository;
import com.resumeradar.port.outbound.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public final class MatchScoringService {

    private final ResumeMatchScorer matchScorer;
    private final JobMatchRepository jobMatchRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ResumeService resumeService;

    public MatchScoringService(ResumeMatchScorer matchScorer,
                               JobMatchRepository jobMatchRepository,
                               JobRepository jobRepository,
                               UserRepository userRepository,
                               ResumeService resumeService) {
        this.matchScorer = matchScorer;
        this.jobMatchRepository = jobMatchRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.resumeService = resumeService;
    }

    @Transactional
    public MatchResult scoreJob(Long userId, Long jobId) {
        Resume resume = resumeService.getActiveResume(userId)
            .orElseThrow(() -> new IllegalStateException("No active resume found. Please upload a resume first."));

        JobEntity jobEntity = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        Job.Listing listing = toJobListing(jobEntity);
        MatchResult result = matchScorer.score(listing, resume);

        saveMatchResult(userId, jobEntity, result);
        return result;
    }

    @Transactional
    public List<MatchResult> scoreJobs(Long userId, List<Long> jobIds) {
        return jobIds.stream()
            .map(jobId -> scoreJob(userId, jobId))
            .toList();
    }

    public Optional<MatchResult> getExistingMatch(Long userId, Long jobId) {
        return jobMatchRepository.findByJobIdAndUserId(jobId, userId)
            .map(this::toMatchResult);
    }

    private void saveMatchResult(Long userId, JobEntity job, MatchResult result) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        JobMatchEntity entity = jobMatchRepository.findByJobIdAndUserId(job.getId(), userId)
            .orElse(new JobMatchEntity());

        entity.setJob(job);
        entity.setUser(user);
        entity.setMatchPercentage(result.matchPercentage());
        entity.setMatchedSkills(String.join(",", result.matchedSkills()));
        entity.setMissingSkills(String.join(",", result.missingSkills()));
        entity.setSummary(result.summary());
        entity.setScoredAt(Instant.now());

        jobMatchRepository.save(entity);
    }

    private MatchResult toMatchResult(JobMatchEntity entity) {
        return new MatchResult(
            entity.getMatchPercentage(),
            List.of(entity.getMatchedSkills().split(",")),
            List.of(entity.getMissingSkills().split(",")),
            entity.getSummary()
        );
    }

    private Job.Listing toJobListing(JobEntity entity) {
        return new Job.Listing(
            entity.getExternalId(),
            entity.getTitle(),
            entity.getCompany(),
            entity.getLocation(),
            entity.getWorkMode(),
            entity.getDescription(),
            entity.getPlatform(),
            entity.getApplicationMode(),
            entity.getPostedAt(),
            entity.getApplyUrl()
        );
    }
}
