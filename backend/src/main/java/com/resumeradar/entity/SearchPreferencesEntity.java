package com.resumeradar.entity;

import com.resumeradar.domain.model.WorkMode;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "search_preferences")
public class SearchPreferencesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    private String jobTitle;
    private String location;

    private boolean remoteEnabled;
    private boolean hybridEnabled;
    private boolean onsiteEnabled;

    private Instant updatedAt;

    public SearchPreferencesEntity() {
        this.remoteEnabled = true;
        this.hybridEnabled = true;
        this.onsiteEnabled = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public boolean isRemoteEnabled() { return remoteEnabled; }
    public void setRemoteEnabled(boolean remoteEnabled) { this.remoteEnabled = remoteEnabled; }
    public boolean isHybridEnabled() { return hybridEnabled; }
    public void setHybridEnabled(boolean hybridEnabled) { this.hybridEnabled = hybridEnabled; }
    public boolean isOnsiteEnabled() { return onsiteEnabled; }
    public void setOnsiteEnabled(boolean onsiteEnabled) { this.onsiteEnabled = onsiteEnabled; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public java.util.List<WorkMode> getActiveWorkModes() {
        java.util.List<WorkMode> modes = new java.util.ArrayList<>();
        if (remoteEnabled) modes.add(WorkMode.REMOTE);
        if (hybridEnabled) modes.add(WorkMode.HYBRID);
        if (onsiteEnabled) modes.add(WorkMode.ONSITE);
        return modes;
    }

    public void reset() {
        this.jobTitle = null;
        this.location = null;
        this.remoteEnabled = true;
        this.hybridEnabled = true;
        this.onsiteEnabled = true;
        this.updatedAt = Instant.now();
    }
}
