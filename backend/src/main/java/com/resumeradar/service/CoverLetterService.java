package com.resumeradar.service;

import com.resumeradar.domain.model.CoverLetter;
import com.resumeradar.domain.model.Job;
import com.resumeradar.domain.model.MatchResult;
import com.resumeradar.domain.model.Resume;
import com.resumeradar.entity.CoverLetterEntity;
import com.resumeradar.entity.JobEntity;
import com.resumeradar.entity.UserEntity;
import com.resumeradar.port.inbound.CoverLetterGenerator;
import com.resumeradar.port.outbound.CoverLetterRepository;
import com.resumeradar.port.outbound.JobRepository;
import com.resumeradar.port.outbound.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public final class CoverLetterService {

    private final CoverLetterGenerator generator;
    private final CoverLetterRepository coverLetterRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ResumeService resumeService;
    private final MatchScoringService matchScoringService;

    public CoverLetterService(CoverLetterGenerator generator,
                              CoverLetterRepository coverLetterRepository,
                              JobRepository jobRepository,
                              UserRepository userRepository,
                              ResumeService resumeService,
                              MatchScoringService matchScoringService) {
        this.generator = generator;
        this.coverLetterRepository = coverLetterRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.resumeService = resumeService;
        this.matchScoringService = matchScoringService;
    }

    @Transactional
    public CoverLetterEntity generateForJob(Long userId, Long jobId) {
        Resume resume = resumeService.getActiveResume(userId)
            .orElseThrow(() -> new IllegalStateException("No active resume found"));

        JobEntity jobEntity = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        // Verify match threshold
        MatchResult match = matchScoringService.getExistingMatch(userId, jobId)
            .orElseGet(() -> matchScoringService.scoreJob(userId, jobId));

        if (!match.isEligibleForApplication()) {
            throw new IllegalStateException(
                "Match percentage (" + match.matchPercentage() + "%) is below the 80% threshold");
        }

        Job.Listing listing = new Job.Listing(
            jobEntity.getExternalId(), jobEntity.getTitle(), jobEntity.getCompany(),
            jobEntity.getLocation(), jobEntity.getWorkMode(), jobEntity.getDescription(),
            jobEntity.getPlatform(), jobEntity.getApplicationMode(),
            jobEntity.getPostedAt(), jobEntity.getApplyUrl());

        CoverLetter generated = generator.generate(listing, resume, null);

        UserEntity user = userRepository.findById(userId).orElseThrow();
        CoverLetterEntity entity = new CoverLetterEntity();
        entity.setJob(jobEntity);
        entity.setUser(user);
        entity.setContent(generated.content());
        entity.setGeneratedAt(Instant.now());
        entity.setApproved(false);

        return coverLetterRepository.save(entity);
    }

    @Transactional
    public CoverLetterEntity approveCoverLetter(Long coverLetterId) {
        CoverLetterEntity entity = coverLetterRepository.findById(coverLetterId)
            .orElseThrow(() -> new IllegalArgumentException("Cover letter not found"));
        entity.setApproved(true);
        return coverLetterRepository.save(entity);
    }

    @Transactional
    public CoverLetterEntity regenerateForJob(Long userId, Long jobId) {
        coverLetterRepository.findByJobIdAndUserId(jobId, userId)
            .ifPresent(coverLetterRepository::delete);
        return generateForJob(userId, jobId);
    }
}
