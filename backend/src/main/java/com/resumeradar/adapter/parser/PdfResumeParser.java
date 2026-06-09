package com.resumeradar.adapter.parser;

import com.resumeradar.domain.model.Resume;
import com.resumeradar.port.inbound.ResumeParser;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public final class PdfResumeParser implements ResumeParser {

    private static final Pattern SKILLS_SECTION = Pattern.compile(
        "(?i)(skills|technologies|technical skills|core competencies)[:\\s]*([\\s\\S]*?)(?=\\n\\n|$)");

    @Override
    public Resume parse(byte[] fileContent, String fileName) {
        String text = extractText(fileContent);
        List<String> skills = extractSkills(text);
        return new Resume(null, fileName, text, skills, Instant.now());
    }

    @Override
    public boolean supports(String mimeType) {
        return "application/pdf".equals(mimeType);
    }

    private String extractText(byte[] content) {
        try (PDDocument document = Loader.loadPDF(content)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse PDF resume", e);
        }
    }

    private List<String> extractSkills(String text) {
        Matcher matcher = SKILLS_SECTION.matcher(text);
        if (matcher.find()) {
            String skillsText = matcher.group(2).trim();
            return Arrays.stream(skillsText.split("[,;|\\n•\\-]"))
                .map(String::trim)
                .filter(s -> !s.isBlank() && s.length() < 50)
                .toList();
        }
        return List.of();
    }
}
