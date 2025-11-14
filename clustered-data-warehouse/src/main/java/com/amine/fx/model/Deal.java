package com.amine.fx.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fx_deals", uniqueConstraints = {
        @UniqueConstraint(columnNames = "deal_unique_id")  // 🚀 Prevents duplicate imports
})
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Deal Unique ID is required")
    @Column(name = "deal_unique_id", nullable = false, unique = true)
    private String dealUniqueId;

    @NotBlank(message = "From Currency is required")
    @Size(min = 3, max = 3, message = "From Currency must be exactly 3 characters")
    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency;

    @NotBlank(message = "To Currency is required")
    @Size(min = 3, max = 3, message = "To Currency must be exactly 3 characters")
    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrency;

    @NotNull(message = "Deal timestamp is required")
    @Column(name = "deal_timestamp", nullable = false)
    private LocalDateTime dealTimestamp;

    @NotNull(message = "Deal amount is required")
    @DecimalMin(value = "0.01", message = "Deal amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    // 🚀 Removed receivedAt since we only need deal timestamp from CSV

    // Default constructor
    public Deal() {
        // JPA requires default constructor
    }

    // Parameterized constructor
    public Deal(String dealUniqueId, String fromCurrency, String toCurrency,
                LocalDateTime dealTimestamp, BigDecimal amount) {
        this.dealUniqueId = dealUniqueId;
        this.fromCurrency = cleanCurrencyCode(fromCurrency);  // 🚀 Clean spaces
        this.toCurrency = cleanCurrencyCode(toCurrency);
        this.dealTimestamp = dealTimestamp;
        this.amount = amount;
    }

    // 🚀 Helper method to clean currency codes (handles " USD" -> "USD")
    private String cleanCurrencyCode(String currency) {
        if (currency == null) {
            return null;
        }
        return currency.trim().toUpperCase();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDealUniqueId() {
        return dealUniqueId;
    }

    public void setDealUniqueId(String dealUniqueId) {
        this.dealUniqueId = dealUniqueId;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = cleanCurrencyCode(fromCurrency);
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(String toCurrency) {
        this.toCurrency = cleanCurrencyCode(toCurrency);
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

    @Override
    public String toString() {
        return "Deal{" +
                "dealUniqueId='" + dealUniqueId + '\'' +
                ", fromCurrency='" + fromCurrency + '\'' +
                ", toCurrency='" + toCurrency + '\'' +
                ", dealTimestamp=" + dealTimestamp +
                ", amount=" + amount +
                '}';
    }
}