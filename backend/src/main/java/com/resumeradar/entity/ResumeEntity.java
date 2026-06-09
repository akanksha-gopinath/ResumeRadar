package com.resumeradar.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "resumes")
public class ResumeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private String fileName;

    @Lob
    private byte[] fileContent;

    @Column(columnDefinition = "TEXT")
    private String parsedText;

    private String skills;
    private Instant uploadedAt;
    private boolean active;

    public ResumeEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public byte[] getFileContent() { return fileContent; }
    public void setFileContent(byte[] fileContent) { this.fileContent = fileContent; }
    public String getParsedText() { return parsedText; }
    public void setParsedText(String parsedText) { this.parsedText = parsedText; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
