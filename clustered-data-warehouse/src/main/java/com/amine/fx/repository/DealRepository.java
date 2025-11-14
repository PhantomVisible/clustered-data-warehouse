package com.amine.fx.repository;

import com.amine.fx.model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // 🎯 Tells Spring: "This manages database operations for Deal"
public interface DealRepository extends JpaRepository<Deal, Long> {  // 🚀 Changed to Long (matching Deal.id)

    // ❓ Check if a deal with this ID already exists
    boolean existsByDealUniqueId(String dealUniqueId);  // 🚀 Changed from dealId to dealUniqueId

    // 🔍 Find a deal by its unique ID
    Optional<Deal> findByDealUniqueId(String dealUniqueId);

    // JpaRepository already provides: save(), findById(), findAll(), delete(), etc.
}