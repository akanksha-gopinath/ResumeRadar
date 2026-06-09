package com.resumeradar.controller;

import com.resumeradar.entity.ApplicationEntity;
import com.resumeradar.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ApplicationService applicationService;

    public DashboardController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApplicationService.DashboardStats> getStats(@RequestParam("userId") Long userId) {
        ApplicationService.DashboardStats stats = applicationService.getDashboardStats(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/applications")
    public ResponseEntity<List<ApplicationEntity>> getAllApplications(@RequestParam("userId") Long userId) {
        List<ApplicationEntity> applications = applicationService.getUserApplications(userId);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ApplicationEntity>> getRecentApplications(@RequestParam("userId") Long userId) {
        List<ApplicationEntity> recent = applicationService.getRecentApplications(userId);
        return ResponseEntity.ok(recent);
    }
}
