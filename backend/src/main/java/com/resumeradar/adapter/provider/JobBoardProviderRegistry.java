package com.resumeradar.adapter.provider;

import com.resumeradar.port.inbound.ApplicationSubmitter;
import com.resumeradar.port.inbound.JobSearchProvider;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public final class JobBoardProviderRegistry {

    private final Map<String, JobSearchProvider> searchProviders;
    private final Map<String, ApplicationSubmitter> submitters;

    public JobBoardProviderRegistry(List<JobSearchProvider> searchProviders,
                                    List<ApplicationSubmitter> submitters) {
        this.searchProviders = searchProviders.stream()
            .collect(Collectors.toUnmodifiableMap(
                JobSearchProvider::providerName, Function.identity()));
        this.submitters = submitters.stream()
            .collect(Collectors.toUnmodifiableMap(
                ApplicationSubmitter::providerName, Function.identity()));
    }

    public JobSearchProvider getSearchProvider(String name) {
        return Optional.ofNullable(searchProviders.get(name))
            .orElseThrow(() -> new UnsupportedOperationException(
                "No search provider registered for: " + name));
    }

    public ApplicationSubmitter getSubmitter(String name) {
        return Optional.ofNullable(submitters.get(name))
            .orElseThrow(() -> new UnsupportedOperationException(
                "No application submitter registered for: " + name));
    }

    public Set<String> availableProviders() {
        return Collections.unmodifiableSet(searchProviders.keySet());
    }
}
