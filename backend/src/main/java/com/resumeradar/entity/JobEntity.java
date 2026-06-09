package com.resumeradar.entity;

import com.resumeradar.domain.model.ApplicationMode;
import com.resumeradar.domain.model.WorkMode;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "jobs")
public class JobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String externalId;

    private String title;
    private String company;
    private String location;

    @Enumerated(EnumType.STRING)
    private WorkMode workMode;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String platform;
    private String applyUrl;

    @Enumerated(EnumType.STRING)
    private ApplicationMode applicationMode;

    private Instant postedAt;
    private Instant discoveredAt;

    public JobEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public WorkMode getWorkMode() { return workMode; }
    public void setWorkMode(WorkMode workMode) { this.workMode = workMode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getApplyUrl() { return applyUrl; }
    public void setApplyUrl(String applyUrl) { this.applyUrl = applyUrl; }
    public ApplicationMode getApplicationMode() { return applicationMode; }
    public void setApplicationMode(ApplicationMode applicationMode) { this.applicationMode = applicationMode; }
    public Instant getPostedAt() { return postedAt; }
    public void setPostedAt(Instant postedAt) { this.postedAt = postedAt; }
    public Instant getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(Instant discoveredAt) { this.discoveredAt = discoveredAt; }
}
