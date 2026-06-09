package com.resumeradar.controller;

import com.resumeradar.entity.SearchPreferencesEntity;
import com.resumeradar.entity.UserEntity;
import com.resumeradar.port.outbound.SearchPreferencesRepository;
import com.resumeradar.port.outbound.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/preferences")
public class SearchPreferencesController {

    private final SearchPreferencesRepository preferencesRepository;
    private final UserRepository userRepository;

    public SearchPreferencesController(SearchPreferencesRepository preferencesRepository,
                                       UserRepository userRepository) {
        this.preferencesRepository = preferencesRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<SearchPreferencesEntity> getPreferences(@RequestParam("userId") Long userId) {
        return preferencesRepository.findByUserId(userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<SearchPreferencesEntity> updatePreferences(
            @RequestParam("userId") Long userId,
            @RequestBody UpdatePreferencesRequest request) {

        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SearchPreferencesEntity prefs = preferencesRepository.findByUserId(userId)
            .orElseGet(() -> {
                SearchPreferencesEntity newPrefs = new SearchPreferencesEntity();
                newPrefs.setUser(user);
                return newPrefs;
            });

        if (request.jobTitle() != null) prefs.setJobTitle(request.jobTitle());
        if (request.location() != null) prefs.setLocation(request.location());
        if (request.remoteEnabled() != null) prefs.setRemoteEnabled(request.remoteEnabled());
        if (request.hybridEnabled() != null) prefs.setHybridEnabled(request.hybridEnabled());
        if (request.onsiteEnabled() != null) prefs.setOnsiteEnabled(request.onsiteEnabled());
        prefs.setUpdatedAt(Instant.now());

        return ResponseEntity.ok(preferencesRepository.save(prefs));
    }

    @PostMapping("/reset")
    public ResponseEntity<SearchPreferencesEntity> resetPreferences(@RequestParam("userId") Long userId) {
        SearchPreferencesEntity prefs = preferencesRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("No preferences found"));

        prefs.reset();
        return ResponseEntity.ok(preferencesRepository.save(prefs));
    }

    record UpdatePreferencesRequest(
        String jobTitle,
        String location,
        Boolean remoteEnabled,
        Boolean hybridEnabled,
        Boolean onsiteEnabled
    ) {}
}
