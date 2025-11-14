package com.amine.fx.service;

import com.amine.fx.model.Deal;
import com.amine.fx.model.DealError;
import com.amine.fx.repository.DealRepository;
import com.amine.fx.repository.DealErrorRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DealImportService {

    private static final Logger logger = LoggerFactory.getLogger(DealImportService.class);

    // 🚀 DateTime formatter for parsing CSV timestamp
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME; // Handles "2025-11-13T10:00:00"

    private final DealRepository dealRepository;
    private final DealErrorRepository dealErrorRepository;

    @Autowired
    public DealImportService(DealRepository dealRepository, DealErrorRepository dealErrorRepository) {
        this.dealRepository = dealRepository;
        this.dealErrorRepository = dealErrorRepository;
    }

    /**
     * Import deals from CSV file with validation and error handling
     * No rollback - each row is processed independently
     */
    /**
     * Import deals from CSV file with validation and error handling
     * No rollback - each row is processed independently
     */
    @Transactional
    public void importDealsFromCsv(String filePath) {
        logger.info("Starting CSV import from file: {}", filePath);

        Path path = Path.of(filePath);

        // 🚀 ADD THIS CHECK FOR MISSING FILE
        if (!Files.exists(path)) {
            logger.error("CSV file not found: {}", filePath);
            System.err.println("❌ CSV file not found: " + filePath);
            return; // Exit gracefully instead of throwing exception
        }

        int successCount = 0;
        int errorCount = 0;

        try (CSVParser parser = CSVParser.parse(path,
                java.nio.charset.StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {

            for (CSVRecord record : parser) {
                try {
                    processDealRecord(record);
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    logger.error("Failed to process record {}: {}", record.getRecordNumber(), e.getMessage());
                    // 🚀 Continue to next record - no rollback!
                }
            }

        } catch (IOException e) {
            logger.error("Failed to read CSV file: {}", filePath, e);
            throw new RuntimeException("CSV file reading failed: " + e.getMessage(), e);
        }

        logger.info("CSV import completed. Successful: {}, Failed: {}", successCount, errorCount);
        System.out.println("✅ CSV import completed. Successful: " + successCount + ", Failed: " + errorCount);
    }

    /**
     * Process a single deal record with validation and persistence
     */
    private void processDealRecord(CSVRecord record) {
        String dealUniqueId = record.get("Deal Unique Id");
        String fromCurrency = record.get("From Currency ISO Code");
        String toCurrency = record.get("To Currency ISO Code");
        String timestampStr = record.get("Deal timestamp");
        String amountStr = record.get("Deal Amount");

        logger.debug("Processing deal: {}", dealUniqueId);

        // 🚀 Step 1: Basic validation - check for empty required fields
        if (isBlank(dealUniqueId) || isBlank(fromCurrency) || isBlank(toCurrency) ||
                isBlank(timestampStr) || isBlank(amountStr)) {
            saveDealError(dealUniqueId, fromCurrency, toCurrency, timestampStr, amountStr,
                    "Missing required fields");
            return;
        }

        // 🚀 Step 2: Check for duplicate deal
        if (dealRepository.existsByDealUniqueId(dealUniqueId)) {
            saveDealError(dealUniqueId, fromCurrency, toCurrency, timestampStr, amountStr,
                    "Duplicate deal ID");
            return;
        }

        // 🚀 Step 3: Parse and validate timestamp
        LocalDateTime dealTimestamp;
        try {
            dealTimestamp = LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
        } catch (DateTimeParseException e) {
            saveDealError(dealUniqueId, fromCurrency, toCurrency, timestampStr, amountStr,
                    "Invalid timestamp format. Expected: YYYY-MM-DDTHH:MM:SS");
            return;
        }

        // 🚀 Step 4: Parse and validate amount
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr.trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                saveDealError(dealUniqueId, fromCurrency, toCurrency, timestampStr, amountStr,
                        "Deal amount must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            saveDealError(dealUniqueId, fromCurrency, toCurrency, timestampStr, amountStr,
                    "Invalid amount format");
            return;
        }

        // 🚀 Step 5: Validate currency codes
        if (fromCurrency.trim().length() != 3 || toCurrency.trim().length() != 3) {
            saveDealError(dealUniqueId, fromCurrency, toCurrency, timestampStr, amountStr,
                    "Currency codes must be exactly 3 characters");
            return;
        }

        // 🚀 Step 6: Save the valid deal
        try {
            Deal deal = new Deal(dealUniqueId, fromCurrency, toCurrency, dealTimestamp, amount);
            dealRepository.save(deal);
            logger.info("Successfully saved deal: {}", dealUniqueId);
        } catch (Exception e) {
            saveDealError(dealUniqueId, fromCurrency, toCurrency, timestampStr, amountStr,
                    "Database error: " + e.getMessage());
        }
    }

    /**
     * Save error record for failed deal processing
     */
    private void saveDealError(String dealUniqueId, String fromCurrency, String toCurrency,
                               String timestampStr, String amountStr, String errorReason) {
        try {
            LocalDateTime dealTimestamp = null;
            try {
                dealTimestamp = LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
            } catch (DateTimeParseException e) {
                // Keep as null if timestamp is invalid
            }

            DealError error = new DealError(dealUniqueId, fromCurrency, toCurrency,
                    dealTimestamp, amountStr, errorReason);
            dealErrorRepository.save(error);
            logger.warn("Saved deal error: {} - {}", dealUniqueId, errorReason);
        } catch (Exception e) {
            logger.error("Failed to save deal error for {}: {}", dealUniqueId, e.getMessage());
        }
    }

    /**
     * Helper method to check if string is blank (null or empty)
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}