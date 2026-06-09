package com.resumeradar.port.inbound;

import com.resumeradar.domain.model.ApplicationMode;
import com.resumeradar.domain.model.CoverLetter;
import com.resumeradar.domain.model.Job;
import com.resumeradar.domain.model.Resume;

public interface ApplicationSubmitter {

    String providerName();

    ApplicationMode mode();

    SubmissionResult submit(Job.Listing job, CoverLetter coverLetter, Resume resume);

    boolean supports(Job.Listing job);

    record SubmissionResult(boolean success, String message, String confirmationId) {}
}
