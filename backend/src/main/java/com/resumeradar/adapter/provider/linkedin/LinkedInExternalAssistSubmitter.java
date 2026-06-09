package com.resumeradar.adapter.provider.linkedin;

import com.resumeradar.domain.model.ApplicationMode;
import com.resumeradar.domain.model.CoverLetter;
import com.resumeradar.domain.model.Job;
import com.resumeradar.domain.model.Resume;
import com.resumeradar.port.inbound.ApplicationSubmitter;
import org.springframework.stereotype.Component;

@Component
public class LinkedInExternalAssistSubmitter implements ApplicationSubmitter {

    @Override
    public String providerName() {
        return "linkedin-external-assist";
    }

    @Override
    public ApplicationMode mode() {
        return ApplicationMode.EXTERNAL_ASSISTED;
    }

    @Override
    public SubmissionResult submit(Job.Listing job, CoverLetter coverLetter, Resume resume) {
        // For external-assisted mode, we don't actually submit.
        // The frontend opens the URL and copies cover letter to clipboard.
        // This records the intent and returns the URL + letter for the frontend to act on.
        return new SubmissionResult(
            true,
            "Ready for manual application. Career page URL: " + job.applyUrl(),
            null
        );
    }

    @Override
    public boolean supports(Job.Listing job) {
        return job.applicationMode() == ApplicationMode.EXTERNAL_ASSISTED;
    }
}
