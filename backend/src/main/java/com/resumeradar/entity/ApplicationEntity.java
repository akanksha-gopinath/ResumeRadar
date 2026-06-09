package com.resumeradar.entity;

import com.resumeradar.domain.model.ApplicationMode;
import com.resumeradar.domain.model.ApplicationStatus;
import com.resumeradar.domain.model.ApprovalStage;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "applications")
public class ApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobEntity job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_letter_id")
    private CoverLetterEntity coverLetter;

    @Enumerated(EnumType.STRING)
    private ApprovalStage currentStage;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    private ApplicationMode applicationMode;

    private Instant createdAt;
    private Instant submittedAt;
    private String failureReason;

    public ApplicationEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public JobEntity getJob() { return job; }
    public void setJob(JobEntity job) { this.job = job; }
    public CoverLetterEntity getCoverLetter() { return coverLetter; }
    public void setCoverLetter(CoverLetterEntity coverLetter) { this.coverLetter = coverLetter; }
    public ApprovalStage getCurrentStage() { return currentStage; }
    public void setCurrentStage(ApprovalStage currentStage) { this.currentStage = currentStage; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public ApplicationMode getApplicationMode() { return applicationMode; }
    public void setApplicationMode(ApplicationMode applicationMode) { this.applicationMode = applicationMode; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
