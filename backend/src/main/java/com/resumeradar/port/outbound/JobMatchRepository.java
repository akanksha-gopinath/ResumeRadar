package com.resumeradar.port.outbound;

import com.resumeradar.entity.JobMatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobMatchRepository extends JpaRepository<JobMatchEntity, Long> {

    Optional<JobMatchEntity> findByJobIdAndUserId(Long jobId, Long userId);

    List<JobMatchEntity> findByUserIdAndMatchPercentageGreaterThanEqual(Long userId, int minMatch);
}
