package com.resumeradar.adapter.ai;

import com.resumeradar.domain.model.CoverLetter;
import com.resumeradar.domain.model.Job;
import com.resumeradar.domain.model.Resume;
import com.resumeradar.port.inbound.CoverLetterGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public final class ClaudeCoverLetterGenerator implements CoverLetterGenerator {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;

    public ClaudeCoverLetterGenerator(RestTemplate restTemplate,
                                      @Value("${anthropic.api-key}") String apiKey,
                                      @Value("${anthropic.model:claude-sonnet-4-6-20250514}") String model) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public CoverLetter generate(Job.Listing job, Resume resume, String additionalContext) {
        String systemPrompt = """
            You are an expert career coach and professional writer.
            Generate a compelling, personalized cover letter that:
            - Directly addresses the job requirements
            - Highlights relevant experience from the resume
            - Uses a professional but personable tone
            - Is concise (3-4 paragraphs, under 400 words)
            - Does NOT use generic filler phrases
            - Includes specific examples from the candidate's background
            Respond ONLY with the cover letter text, no preamble or explanation.""";

        String userPrompt = buildUserPrompt(job, resume, additionalContext);

        String content = callClaudeApi(systemPrompt, userPrompt);
        return new CoverLetter(content, Instant.now());
    }

    private String callClaudeApi(String systemPrompt, String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> body = Map.of(
            "model", model,
            "max_tokens", 2048,
            "system", systemPrompt,
            "messages", List.of(
                Map.of("role", "user", "content", userPrompt)
            )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            "https://api.anthropic.com/v1/messages",
            HttpMethod.POST, request, Map.class);

        Map responseBody = response.getBody();
        List<Map<String, Object>> contentBlocks = (List<Map<String, Object>>) responseBody.get("content");
        return contentBlocks.stream()
            .filter(block -> "text".equals(block.get("type")))
            .map(block -> (String) block.get("text"))
            .reduce("", String::concat);
    }

    private String buildUserPrompt(Job.Listing job, Resume resume, String additionalContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Job Details\n");
        sb.append("Title: ").append(job.title()).append("\n");
        sb.append("Company: ").append(job.company()).append("\n");
        sb.append("Location: ").append(job.location()).append("\n");
        sb.append("Description:\n").append(job.description()).append("\n\n");
        sb.append("## Candidate Resume\n");
        sb.append(resume.parsedText()).append("\n");
        if (additionalContext != null && !additionalContext.isBlank()) {
            sb.append("\n## Additional Context\n").append(additionalContext);
        }
        return sb.toString();
    }
}
