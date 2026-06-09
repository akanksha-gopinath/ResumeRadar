package com.resumeradar.adapter.provider.linkedin;

import com.resumeradar.domain.model.Job;
import com.resumeradar.domain.model.JobSearchCriteria;
import com.resumeradar.port.inbound.JobSearchProvider;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class LinkedInJobSearchProvider implements JobSearchProvider {

    private final LinkedInApiClient apiClient;
    private final LinkedInTokenProvider tokenProvider;

    public LinkedInJobSearchProvider(LinkedInApiClient apiClient,
                                     LinkedInTokenProvider tokenProvider) {
        this.apiClient = apiClient;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public String providerName() {
        return "linkedin";
    }

    @Override
    public List<Job.Listing> search(JobSearchCriteria criteria) {
        String token = tokenProvider.getAccessToken();
        return apiClient.searchJobs(criteria, token);
    }

    @Override
    public List<Job.Listing> searchRecent(JobSearchCriteria criteria, Duration lookback) {
        String token = tokenProvider.getAccessToken();
        return apiClient.searchRecentJobs(criteria, lookback, token);
    }
}
