package com.resumeradar.adapter.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeradar.domain.model.Job;
import com.resumeradar.domain.model.MatchResult;
import com.resumeradar.domain.model.Resume;
import com.resumeradar.port.inbound.ResumeMatchScorer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public final class ClaudeMatchScorer implements ResumeMatchScorer {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public ClaudeMatchScorer(RestTemplate restTemplate,
                             ObjectMapper objectMapper,
                             @Value("${anthropic.api-key}") String apiKey,
                             @Value("${anthropic.model:claude-sonnet-4-6-20250514}") String model) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public MatchResult score(Job.Listing job, Resume resume) {
        String systemPrompt = """
            You are a recruitment matching expert. Analyze how well a candidate's resume
            matches a job posting. Return your analysis as JSON only, with this exact structure:
            {
              "matchPercentage": <0-100 integer>,
              "matchedSkills": ["skill1", "skill2"],
              "missingSkills": ["skill1", "skill2"],
              "summary": "<2-3 sentence explanation>"
            }

            Scoring criteria:
            - Required skills match: 40% weight
            - Experience level alignment: 25% weight
            - Domain/industry relevance: 20% weight
            - Education/certification match: 15% weight

            Be strict and realistic. Only give >80% if the candidate is genuinely well-qualified.
            Respond with JSON only, no markdown formatting or code blocks.""";

        String userPrompt = buildMatchPrompt(job, resume);
        String responseText = callClaudeApi(systemPrompt, userPrompt);

        return parseMatchResult(responseText);
    }

    private String callClaudeApi(String systemPrompt, String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> body = Map.of(
            "model", model,
            "max_tokens", 1024,
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

    private MatchResult parseMatchResult(String json) {
        try {
            var node = objectMapper.readTree(json);
            int percentage = node.get("matchPercentage").asInt();
            List<String> matched = objectMapper.convertValue(
                node.get("matchedSkills"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            List<String> missing = objectMapper.convertValue(
                node.get("missingSkills"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            String summary = node.get("summary").asText();
            return new MatchResult(percentage, matched, missing, summary);
        } catch (JsonProcessingException e) {
            return new MatchResult(0, List.of(), List.of(), "Failed to parse match score");
        }
    }

    private String buildMatchPrompt(Job.Listing job, Resume resume) {
        return """
            ## Job Posting
            Title: %s
            Company: %s
            Location: %s
            Description:
            %s

            ## Candidate Resume
            %s
            """.formatted(job.title(), job.company(), job.location(),
                         job.description(), resume.parsedText());
    }
}
