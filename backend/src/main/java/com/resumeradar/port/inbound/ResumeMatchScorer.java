package com.resumeradar.port.inbound;

import com.resumeradar.domain.model.Job;
import com.resumeradar.domain.model.MatchResult;
import com.resumeradar.domain.model.Resume;

public interface ResumeMatchScorer {

    MatchResult score(Job.Listing job, Resume resume);
}
