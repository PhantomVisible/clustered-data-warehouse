package com.amine.fx.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fx_deal_errors") // 🗄️ Different table for errors
public class DealError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Store all the original data that failed

    @Column(name = "deal_unique_id")
    private String dealUniqueId; // 🔸 Optional - might not have it

    @Column(name = "from_currency", length = 3)
    private String fromCurrency;

    @Column(name = "to_currency", length = 3)
    private String toCurrency;

    @Column(name = "deal_timestamp")
    private LocalDateTime dealTimestamp;

    @Column(name = "amount", precision = 19, scale = 4)
    private String amount; // 🚀 Store as String to preserve original format

    @Column(name = "error_reason", nullable = false, length = 500) // ❓ Why it failed
    private String errorReason;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt; // ⏰ When the error happened

    // Default constructor
    public DealError() {
        this.occurredAt = LocalDateTime.now(); // 🕒 Auto-set error time
    }

    // Special constructor for validation errors
    public DealError(String dealUniqueId, String fromCurrency, String toCurrency,
                     LocalDateTime dealTimestamp, String amount, String errorReason) {
        this(); // Call default constructor
        // Store all the original data
        this.dealUniqueId = dealUniqueId;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.dealTimestamp = dealTimestamp;
        this.amount = amount;
        this.errorReason = errorReason;
    }

    // Constructor for duplicate errors
    public DealError(String dealUniqueId, String errorReason) {
        this();
        this.dealUniqueId = dealUniqueId;
        this.errorReason = errorReason;
    }


    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDealUniqueId() { return dealUniqueId; }
    public void setDealUniqueId(String dealUniqueId) { this.dealUniqueId = dealUniqueId; }

    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }

    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }

    public LocalDateTime getDealTimestamp() { return dealTimestamp; }
    public void setDealTimestamp(LocalDateTime dealTimestamp) { this.dealTimestamp = dealTimestamp; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getErrorReason() { return errorReason; }
    public void setErrorReason(String errorReason) { this.errorReason = errorReason; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

    @Override
    public String toString() {
        return "DealError{" +
                "dealUniqueId='" + dealUniqueId + '\'' +
                ", errorReason='" + errorReason + '\'' +
                ", occurredAt=" + occurredAt +
                '}';
    }
}