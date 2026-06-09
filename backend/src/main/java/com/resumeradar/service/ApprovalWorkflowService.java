package com.resumeradar.service;

import com.resumeradar.domain.model.ApprovalStage;
import com.resumeradar.entity.ApplicationEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public final class ApprovalWorkflowService {

    private static final Map<ApprovalStage, Set<ApprovalStage>> TRANSITIONS = Map.of(
        ApprovalStage.SEARCH_RESULTS_PENDING, Set.of(ApprovalStage.MATCH_SCORED),
        ApprovalStage.MATCH_SCORED, Set.of(ApprovalStage.JOBS_SELECTED),
        ApprovalStage.JOBS_SELECTED, Set.of(ApprovalStage.COVER_LETTER_PENDING),
        ApprovalStage.COVER_LETTER_PENDING, Set.of(ApprovalStage.COVER_LETTER_REVIEWED, ApprovalStage.COVER_LETTER_PENDING),
        ApprovalStage.COVER_LETTER_REVIEWED, Set.of(ApprovalStage.APPLY_CONFIRMED),
        ApprovalStage.APPLY_CONFIRMED, Set.of(ApprovalStage.APPLIED)
    );

    public void transition(ApplicationEntity application, ApprovalStage targetStage) {
        ApprovalStage current = application.getCurrentStage();
        Set<ApprovalStage> allowed = TRANSITIONS.getOrDefault(current, Set.of());

        if (!allowed.contains(targetStage)) {
            throw new IllegalStateException(
                "Invalid transition from " + current + " to " + targetStage);
        }
        application.setCurrentStage(targetStage);
    }

    public boolean canTransition(ApprovalStage from, ApprovalStage to) {
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }
}
