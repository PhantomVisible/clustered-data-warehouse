package com.amine.fx.repository;

import com.amine.fx.model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DealRepository extends JpaRepository<Deal, String> {
    boolean existsByDealUniqueIdAndDealTimestamp(String dealUniqueId, LocalDateTime dealTimestamp);
    // JpaRepository already gives us:
    // - save()
    // - findById()
    // - findAll()
    // etc.
}
