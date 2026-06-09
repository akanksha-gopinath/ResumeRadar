package com.resumeradar.port.outbound;

import com.resumeradar.entity.CoverLetterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoverLetterRepository extends JpaRepository<CoverLetterEntity, Long> {

    Optional<CoverLetterEntity> findByJobIdAndUserId(Long jobId, Long userId);
}
