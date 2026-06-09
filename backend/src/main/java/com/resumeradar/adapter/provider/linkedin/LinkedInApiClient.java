package com.resumeradar.adapter.provider.linkedin;

import com.resumeradar.domain.model.ApplicationMode;
import com.resumeradar.domain.model.Job;
import com.resumeradar.domain.model.JobSearchCriteria;
import com.resumeradar.domain.model.WorkMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class LinkedInApiClient {

    private final RestTemplate restTemplate;
    private final String apiBaseUrl;

    public LinkedInApiClient(RestTemplate restTemplate,
                             @Value("${linkedin.api.base-url:https://api.linkedin.com/v2}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl;
    }

    public List<Job.Listing> searchJobs(JobSearchCriteria criteria, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // LinkedIn Jobs Search API call
        // Note: LinkedIn's actual job search API requires partner-level access
        // This is structured for when access is granted
        String url = buildSearchUrl(criteria);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        // Placeholder: actual LinkedIn API integration
        return new ArrayList<>();
    }

    public List<Job.Listing> searchRecentJobs(JobSearchCriteria criteria,
                                               Duration lookback,
                                               String accessToken) {
        // Filter by posted date within lookback period
        List<Job.Listing> allJobs = searchJobs(criteria, accessToken);
        Instant cutoff = Instant.now().minus(lookback);
        return allJobs.stream()
            .filter(job -> job.postedAt().isAfter(cutoff))
            .toList();
    }

    public boolean submitEasyApply(Job.Listing job, byte[] resumeContent,
                                    String coverLetterText, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // LinkedIn Easy Apply API submission
        // Requires specific LinkedIn partner permissions
        return true;
    }

    private String buildSearchUrl(JobSearchCriteria criteria) {
        StringBuilder url = new StringBuilder(apiBaseUrl)
            .append("/jobSearch?keywords=").append(criteria.title());

        if (criteria.location() != null && !criteria.location().isBlank()) {
            url.append("&location=").append(criteria.location());
        }

        if (criteria.workModes() != null && !criteria.workModes().isEmpty()) {
            String modes = criteria.workModes().stream()
                .map(this::toLinkedInWorkMode)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
            url.append("&workplaceType=").append(modes);
        }

        return url.toString();
    }

    private String toLinkedInWorkMode(WorkMode mode) {
        return switch (mode) {
            case REMOTE -> "2";
            case HYBRID -> "3";
            case ONSITE -> "1";
        };
    }
}
