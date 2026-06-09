package com.resumeradar.service;

import com.resumeradar.adapter.provider.JobBoardProviderRegistry;
import com.resumeradar.domain.model.*;
import com.resumeradar.entity.ApplicationEntity;
import com.resumeradar.entity.CoverLetterEntity;
import com.resumeradar.entity.JobEntity;
import com.resumeradar.entity.UserEntity;
import com.resumeradar.port.inbound.ApplicationSubmitter;
import com.resumeradar.port.outbound.ApplicationRepository;
import com.resumeradar.port.outbound.JobRepository;
import com.resumeradar.port.outbound.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public final class ApplicationService {

    private final JobBoardProviderRegistry registry;
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApprovalWorkflowService approvalWorkflow;
    private final ResumeService resumeService;

    public ApplicationService(JobBoardProviderRegistry registry,
                              ApplicationRepository applicationRepository,
                              JobRepository jobRepository,
                              UserRepository userRepository,
                              ApprovalWorkflowService approvalWorkflow,
                              ResumeService resumeService) {
        this.registry = registry;
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.approvalWorkflow = approvalWorkflow;
        this.resumeService = resumeService;
    }

    @Transactional
    public ApplicationEntity createApplication(Long userId, Long jobId, CoverLetterEntity coverLetter) {
        UserEntity user = userRepository.findById(userId).orElseThrow();
        JobEntity job = jobRepository.findById(jobId).orElseThrow();

        ApplicationEntity app = new ApplicationEntity();
        app.setUser(user);
        app.setJob(job);
        app.setCoverLetter(coverLetter);
        app.setCurrentStage(ApprovalStage.COVER_LETTER_REVIEWED);
        app.setStatus(ApplicationStatus.PENDING_APPROVAL);
        app.setApplicationMode(job.getApplicationMode());
        app.setCreatedAt(Instant.now());

        return applicationRepository.save(app);
    }

    @Transactional
    public ApplicationEntity confirmAndSubmit(Long applicationId) {
        ApplicationEntity app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        approvalWorkflow.transition(app, ApprovalStage.APPLY_CONFIRMED);

        if (app.getApplicationMode() == ApplicationMode.EASY_APPLY) {
            submitEasyApply(app);
        } else {
            markAssistedPending(app);
        }

        return applicationRepository.save(app);
    }

    @Transactional
    public ApplicationEntity confirmAssistedComplete(Long applicationId) {
        ApplicationEntity app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setSubmittedAt(Instant.now());
        approvalWorkflow.transition(app, ApprovalStage.APPLIED);

        return applicationRepository.save(app);
    }

    public List<ApplicationEntity> getUserApplications(Long userId) {
        return applicationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<ApplicationEntity> getRecentApplications(Long userId) {
        Instant since = Instant.now().minus(Duration.ofHours(24));
        return applicationRepository.findRecentByUser(userId, since);
    }

    public DashboardStats getDashboardStats(Long userId) {
        long total = applicationRepository.countByUserId(userId);
        Instant last24h = Instant.now().minus(Duration.ofHours(24));
        long recent = applicationRepository.countRecentByUser(userId, last24h);
        return new DashboardStats(total, recent);
    }

    private void submitEasyApply(ApplicationEntity app) {
        Resume resume = resumeService.getActiveResume(app.getUser().getId())
            .orElseThrow(() -> new IllegalStateException("No active resume"));

        JobEntity jobEntity = app.getJob();
        Job.Listing listing = new Job.Listing(
            jobEntity.getExternalId(), jobEntity.getTitle(), jobEntity.getCompany(),
            jobEntity.getLocation(), jobEntity.getWorkMode(), jobEntity.getDescription(),
            jobEntity.getPlatform(), jobEntity.getApplicationMode(),
            jobEntity.getPostedAt(), jobEntity.getApplyUrl());

        CoverLetter coverLetter = new CoverLetter(
            app.getCoverLetter().getContent(), app.getCoverLetter().getGeneratedAt());

        ApplicationSubmitter submitter = registry.getSubmitter("linkedin-easy-apply");
        ApplicationSubmitter.SubmissionResult result = submitter.submit(listing, coverLetter, resume);

        if (result.success()) {
            app.setStatus(ApplicationStatus.SUBMITTED);
            app.setSubmittedAt(Instant.now());
            approvalWorkflow.transition(app, ApprovalStage.APPLIED);
        } else {
            app.setStatus(ApplicationStatus.FAILED);
            app.setFailureReason(result.message());
        }
    }

    private void markAssistedPending(ApplicationEntity app) {
        app.setStatus(ApplicationStatus.ASSISTED_PENDING);
    }

    public record DashboardStats(long totalApplications, long last24Hours) {}
}
