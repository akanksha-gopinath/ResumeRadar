package com.resumeradar.controller;

import com.resumeradar.domain.model.MatchResult;
import com.resumeradar.entity.ApplicationEntity;
import com.resumeradar.entity.CoverLetterEntity;
import com.resumeradar.service.ApplicationService;
import com.resumeradar.service.CoverLetterService;
import com.resumeradar.service.MatchScoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final CoverLetterService coverLetterService;
    private final MatchScoringService matchScoringService;

    public ApplicationController(ApplicationService applicationService,
                                  CoverLetterService coverLetterService,
                                  MatchScoringService matchScoringService) {
        this.applicationService = applicationService;
        this.coverLetterService = coverLetterService;
        this.matchScoringService = matchScoringService;
    }

    @PostMapping("/select-jobs")
    public ResponseEntity<Map<String, Object>> selectJobs(@RequestBody SelectJobsRequest request) {
        // Verify all selected jobs meet the 80% threshold
        for (Long jobId : request.jobIds()) {
            MatchResult match = matchScoringService.getExistingMatch(request.userId(), jobId)
                .orElseGet(() -> matchScoringService.scoreJob(request.userId(), jobId));

            if (!match.isEligibleForApplication()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Job " + jobId + " does not meet the 80% match threshold",
                    "matchPercentage", match.matchPercentage()
                ));
            }
        }

        return ResponseEntity.ok(Map.of(
            "selectedJobs", request.jobIds(),
            "nextStep", "Generate cover letters for selected jobs"
        ));
    }

    @PostMapping("/{jobId}/generate-letter")
    public ResponseEntity<Map<String, Object>> generateCoverLetter(
            @PathVariable Long jobId,
            @RequestParam("userId") Long userId) {
        CoverLetterEntity letter = coverLetterService.generateForJob(userId, jobId);
        return ResponseEntity.ok(Map.of(
            "coverLetterId", letter.getId(),
            "content", letter.getContent(),
            "generatedAt", letter.getGeneratedAt().toString()
        ));
    }

    @PutMapping("/{id}/approve-letter")
    public ResponseEntity<Map<String, Object>> approveCoverLetter(@PathVariable Long id) {
        CoverLetterEntity approved = coverLetterService.approveCoverLetter(id);

        // Create the application entity
        ApplicationEntity app = applicationService.createApplication(
            approved.getUser().getId(), approved.getJob().getId(), approved);

        return ResponseEntity.ok(Map.of(
            "applicationId", app.getId(),
            "status", app.getStatus().name(),
            "applicationMode", app.getApplicationMode().name(),
            "nextStep", "Confirm to submit application"
        ));
    }

    @PutMapping("/{jobId}/regenerate-letter")
    public ResponseEntity<Map<String, Object>> regenerateCoverLetter(
            @PathVariable Long jobId,
            @RequestParam("userId") Long userId) {
        CoverLetterEntity letter = coverLetterService.regenerateForJob(userId, jobId);
        return ResponseEntity.ok(Map.of(
            "coverLetterId", letter.getId(),
            "content", letter.getContent(),
            "generatedAt", letter.getGeneratedAt().toString()
        ));
    }

    @PostMapping("/{id}/confirm-apply")
    public ResponseEntity<Map<String, Object>> confirmApply(@PathVariable Long id) {
        ApplicationEntity app = applicationService.confirmAndSubmit(id);

        return ResponseEntity.ok(Map.of(
            "applicationId", app.getId(),
            "status", app.getStatus().name(),
            "applicationMode", app.getApplicationMode().name(),
            "applyUrl", app.getJob().getApplyUrl() != null ? app.getJob().getApplyUrl() : "",
            "coverLetter", app.getCoverLetter().getContent()
        ));
    }

    @PostMapping("/{id}/confirm-assisted-complete")
    public ResponseEntity<Map<String, Object>> confirmAssistedComplete(@PathVariable Long id) {
        ApplicationEntity app = applicationService.confirmAssistedComplete(id);
        return ResponseEntity.ok(Map.of(
            "applicationId", app.getId(),
            "status", "SUBMITTED",
            "message", "Application marked as submitted"
        ));
    }

    record SelectJobsRequest(Long userId, List<Long> jobIds) {}
}
