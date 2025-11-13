package com.amine.fx.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fx_deal_errors")
public class DealError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "raw_payload", nullable = false, columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "deal_unique_id")
    private String dealUniqueId;

    @Column(name = "error_reason", nullable = false)
    private String errorReason;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    // Default constructor (required by JPA)
    public DealError() {}

    // Constructor with timestamp
    public DealError(String dealUniqueId, String rawPayload, String errorReason, LocalDateTime occurredAt) {
        this.dealUniqueId = dealUniqueId;
        this.rawPayload = rawPayload;
        this.errorReason = errorReason;
        this.occurredAt = occurredAt;
        System.out.println("🛑 DealError created: " + dealUniqueId + " | Reason: " + errorReason);
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }
    public String getDealUniqueId() { return dealUniqueId; }
    public void setDealUniqueId(String dealUniqueId) { this.dealUniqueId = dealUniqueId; }
    public String getErrorReason() { return errorReason; }
    public void setErrorReason(String errorReason) { this.errorReason = errorReason; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
