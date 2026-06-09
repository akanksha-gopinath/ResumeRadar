package com.resumeradar.service;

import com.resumeradar.domain.model.Resume;
import com.resumeradar.entity.ResumeEntity;
import com.resumeradar.entity.UserEntity;
import com.resumeradar.port.inbound.ResumeParser;
import com.resumeradar.port.outbound.ResumeRepository;
import com.resumeradar.port.outbound.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public final class ResumeService {

    private final ResumeParser resumeParser;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    public ResumeService(ResumeParser resumeParser,
                         ResumeRepository resumeRepository,
                         UserRepository userRepository) {
        this.resumeParser = resumeParser;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Resume uploadResume(Long userId, byte[] fileContent, String fileName) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Resume parsed = resumeParser.parse(fileContent, fileName);

        // Deactivate any existing active resume
        resumeRepository.findByUserIdAndActiveTrue(userId)
            .ifPresent(existing -> {
                existing.setActive(false);
                resumeRepository.save(existing);
            });

        ResumeEntity entity = new ResumeEntity();
        entity.setUser(user);
        entity.setFileName(fileName);
        entity.setFileContent(fileContent);
        entity.setParsedText(parsed.parsedText());
        entity.setSkills(String.join(",", parsed.skills()));
        entity.setUploadedAt(Instant.now());
        entity.setActive(true);

        ResumeEntity saved = resumeRepository.save(entity);
        return toResume(saved);
    }

    public Optional<Resume> getActiveResume(Long userId) {
        return resumeRepository.findByUserIdAndActiveTrue(userId)
            .map(this::toResume);
    }

    private Resume toResume(ResumeEntity entity) {
        return new Resume(
            entity.getId(),
            entity.getFileName(),
            entity.getParsedText(),
            entity.getSkills() != null
                ? java.util.Arrays.asList(entity.getSkills().split(","))
                : java.util.List.of(),
            entity.getUploadedAt()
        );
    }
}
