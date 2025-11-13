package com.amine.fx.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fx_deals")
public class Deal {

    @Id
    @Column(name = "deal_unique_id", nullable = false)
    private String dealId;

    @Column(name = "from_currency", nullable = false)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false)
    private String toCurrency;

    @Column(name = "deal_timestamp", nullable = false)
    private LocalDateTime dealTimestamp;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    public Deal() {
        // Default constructor for JPA
    }

    public Deal(String dealId, String fromCurrency, String toCurrency, LocalDateTime dealTimestamp, BigDecimal amount) {
        this.dealId = dealId;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.dealTimestamp = dealTimestamp;
        this.amount = amount;
        this.receivedAt = LocalDateTime.now();

        // 🎯 Log creation of the deal
        System.out.println("🎯 Deal created: " + this.dealId + " | " + this.fromCurrency + " -> " + this.toCurrency + " | Amount: " + this.amount);
    }

    // Getters and setters
    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }

    public LocalDateTime getDealTimestamp() {
        return dealTimestamp;
    }

    public void setDealTimestamp(LocalDateTime dealTimestamp) {
        this.dealTimestamp = dealTimestamp;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
}
