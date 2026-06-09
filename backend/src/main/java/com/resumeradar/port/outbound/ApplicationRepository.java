package com.resumeradar.port.outbound;

import com.resumeradar.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

    List<ApplicationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT a FROM ApplicationEntity a WHERE a.user.id = :userId " +
           "AND a.submittedAt >= :since ORDER BY a.submittedAt DESC")
    List<ApplicationEntity> findRecentByUser(@Param("userId") Long userId,
                                             @Param("since") Instant since);

    long countByUserId(Long userId);

    @Query("SELECT COUNT(a) FROM ApplicationEntity a WHERE a.user.id = :userId " +
           "AND a.submittedAt >= :since")
    long countRecentByUser(@Param("userId") Long userId, @Param("since") Instant since);
}
