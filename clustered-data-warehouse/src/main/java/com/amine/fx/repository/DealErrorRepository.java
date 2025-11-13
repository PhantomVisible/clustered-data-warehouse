package com.amine.fx.repository;

import com.amine.fx.model.DealError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository for persisting invalid deals into fx_deal_errors table.
 * Extends JpaRepository to get CRUD methods for free.
 */
@Repository
public interface DealErrorRepository extends JpaRepository<DealError, Long> {

    /**
     * Check if a DealError already exists for the given dealUniqueId and occurredAt timestamp.
     * This is used to avoid duplicating error rows for the same deal + timestamp.
     */
    boolean existsByDealUniqueIdAndOccurredAt(String dealUniqueId, LocalDateTime occurredAt);

    // You can later add custom queries if needed, e.g.:
    // List<DealError> findByErrorReason(String reason);
}
