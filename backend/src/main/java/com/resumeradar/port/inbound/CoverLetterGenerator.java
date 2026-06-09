package com.resumeradar.port.inbound;

import com.resumeradar.domain.model.CoverLetter;
import com.resumeradar.domain.model.Job;
import com.resumeradar.domain.model.Resume;

public interface CoverLetterGenerator {

    CoverLetter generate(Job.Listing job, Resume resume, String additionalContext);
}
