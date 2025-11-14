package com.amine.fx.repository;

import com.amine.fx.model.DealError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for persisting invalid deals into fx_deal_errors table.
 */
@Repository
public interface DealErrorRepository extends JpaRepository<DealError, Long> {

    /**
     * Check if an error already exists for the given dealUniqueId
     * This helps avoid logging the same error multiple times
     */
    boolean existsByDealUniqueId(String dealUniqueId);  // 🚀 Simplified - we don't need timestamp check

    // 🚀 You can add more query methods later if needed:
    // List<DealError> findByErrorReasonContaining(String reason);
    // List<DealError> findByOccurredAtAfter(LocalDateTime date);
}