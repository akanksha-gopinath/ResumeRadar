package com.resumeradar.port.inbound;

import com.resumeradar.domain.model.Resume;

public interface ResumeParser {

    Resume parse(byte[] fileContent, String fileName);

    boolean supports(String mimeType);
}
