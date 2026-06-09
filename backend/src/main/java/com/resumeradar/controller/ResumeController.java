package com.resumeradar.controller;

import com.resumeradar.domain.model.Resume;
import com.resumeradar.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        String contentType = file.getContentType();
        if (!"application/pdf".equals(contentType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only PDF files are supported"));
        }

        Resume resume = resumeService.uploadResume(userId, file.getBytes(), file.getOriginalFilename());

        return ResponseEntity.ok(Map.of(
            "id", resume.id(),
            "fileName", resume.fileName(),
            "skills", resume.skills(),
            "uploadedAt", resume.uploadedAt().toString()
        ));
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveResume(@RequestParam("userId") Long userId) {
        return resumeService.getActiveResume(userId)
            .map(resume -> ResponseEntity.ok(Map.<String, Object>of(
                "id", resume.id(),
                "fileName", resume.fileName(),
                "parsedText", resume.parsedText(),
                "skills", resume.skills()
            )))
            .orElse(ResponseEntity.notFound().build());
    }
}
