package com.resumeradar.adapter.provider.linkedin;

import com.resumeradar.domain.model.ApplicationMode;
import com.resumeradar.domain.model.CoverLetter;
import com.resumeradar.domain.model.Job;
import com.resumeradar.domain.model.Resume;
import com.resumeradar.port.inbound.ApplicationSubmitter;
import org.springframework.stereotype.Component;

@Component
public class LinkedInEasyApplySubmitter implements ApplicationSubmitter {

    private final LinkedInApiClient apiClient;
    private final LinkedInTokenProvider tokenProvider;

    public LinkedInEasyApplySubmitter(LinkedInApiClient apiClient,
                                      LinkedInTokenProvider tokenProvider) {
        this.apiClient = apiClient;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public String providerName() {
        return "linkedin-easy-apply";
    }

    @Override
    public ApplicationMode mode() {
        return ApplicationMode.EASY_APPLY;
    }

    @Override
    public SubmissionResult submit(Job.Listing job, CoverLetter coverLetter, Resume resume) {
        String token = tokenProvider.getAccessToken();
        boolean success = apiClient.submitEasyApply(
            job, new byte[0], coverLetter.content(), token);

        if (success) {
            return new SubmissionResult(true, "Application submitted via LinkedIn Easy Apply", null);
        }
        return new SubmissionResult(false, "Failed to submit via LinkedIn Easy Apply", null);
    }

    @Override
    public boolean supports(Job.Listing job) {
        return job.applicationMode() == ApplicationMode.EASY_APPLY
            && "linkedin".equals(job.platform());
    }
}
