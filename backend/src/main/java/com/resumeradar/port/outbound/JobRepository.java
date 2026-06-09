package com.resumeradar.port.outbound;

import com.resumeradar.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, Long> {

    boolean existsByExternalId(String externalId);

    Optional<JobEntity> findByExternalId(String externalId);

    @Query("SELECT j FROM JobEntity j WHERE j.discoveredAt >= :since ORDER BY j.discoveredAt DESC")
    List<JobEntity> findRecentJobs(@Param("since") Instant since);
}
