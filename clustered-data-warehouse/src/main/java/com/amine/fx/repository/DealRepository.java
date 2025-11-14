package com.amine.fx.repository;

import com.amine.fx.model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {  // 🚀 Changed to Long (matching Deal.id)

    /**
     * Check if a deal with the given unique ID already exists
     * This prevents duplicate imports
     */
    boolean existsByDealUniqueId(String dealUniqueId);  // 🚀 Changed from dealId to dealUniqueId

    /**
     * Find a deal by its unique ID
     * Useful for checking duplicates and retrieving specific deals
     */
    Optional<Deal> findByDealUniqueId(String dealUniqueId);

    // 🚀 Removed existsByDealIdAndDealTimestamp - we only need to check by dealUniqueId for duplicates
    // JpaRepository already provides: save(), findById(), findAll(), delete(), etc.
}