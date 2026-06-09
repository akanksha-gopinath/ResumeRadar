package com.resumeradar.port.outbound;

import com.resumeradar.entity.SearchPreferencesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SearchPreferencesRepository extends JpaRepository<SearchPreferencesEntity, Long> {

    Optional<SearchPreferencesEntity> findByUserId(Long userId);
}
