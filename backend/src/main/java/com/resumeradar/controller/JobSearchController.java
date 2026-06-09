package com.resumeradar.controller;

import com.resumeradar.domain.model.Job;
import com.resumeradar.domain.model.JobSearchCriteria;
import com.resumeradar.domain.model.MatchResult;
import com.resumeradar.domain.model.WorkMode;
import com.resumeradar.entity.JobEntity;
import com.resumeradar.service.JobSearchService;
import com.resumeradar.service.MatchScoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobSearchController {

    private final JobSearchService jobSearchService;
    private final MatchScoringService matchScoringService;

    public JobSearchController(JobSearchService jobSearchService,
                               MatchScoringService matchScoringService) {
        this.jobSearchService = jobSearchService;
        this.matchScoringService = matchScoringService;
    }

    @PostMapping("/search")
    public ResponseEntity<List<Job.Listing>> searchJobs(@RequestBody SearchRequest request) {
        JobSearchCriteria criteria = new JobSearchCriteria(
            request.title(), request.location(), request.workModes());
        List<Job.Listing> results = jobSearchService.searchJobs(criteria);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<JobEntity>> getRecentJobs() {
        List<JobEntity> recentJobs = jobSearchService.getRecentlyDiscoveredJobs();
        return ResponseEntity.ok(recentJobs);
    }

    @PostMapping("/{jobId}/match")
    public ResponseEntity<MatchResult> scoreMatch(
            @PathVariable Long jobId,
            @RequestParam("userId") Long userId) {
        MatchResult result = matchScoringService.scoreJob(userId, jobId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/batch-match")
    public ResponseEntity<List<MatchResult>> batchScoreMatch(
            @RequestBody BatchMatchRequest request) {
        List<MatchResult> results = matchScoringService.scoreJobs(request.userId(), request.jobIds());
        return ResponseEntity.ok(results);
    }

    record SearchRequest(String title, String location, List<WorkMode> workModes) {}
    record BatchMatchRequest(Long userId, List<Long> jobIds) {}
}
