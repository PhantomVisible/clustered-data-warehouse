package com.amine.fx.repository;

import com.amine.fx.model.DealError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//Repository for persisting invalid deals into fx_deal_errors table.
@Repository
public interface DealErrorRepository extends JpaRepository<DealError, Long> {

    // ❓ Check if we already logged an error for this deal
    boolean existsByDealUniqueId(String dealUniqueId);  // 🚀 Simplified - we don't need timestamp check

    // Also gets free CRUD methods from JpaRepository
}