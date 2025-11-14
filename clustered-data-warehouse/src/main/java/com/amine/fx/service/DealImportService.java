package com.amine.fx.service;

// Database models - our ingredients
import com.amine.fx.model.Deal;
import com.amine.fx.model.DealError;

// Database managers - our storage
import com.amine.fx.repository.DealRepository;
import com.amine.fx.repository.DealErrorRepository;

// CSV reading tools - our order ticket reader
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

// Logging - our intercom
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Spring annotations - instructions
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// File handling - our order ticket folder
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;


// Date and number parsing - our timer and scale
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service // 🎯 Tells Spring: "This contains business logic"
public class DealImportService {

    // 📢 intercom for logging messages
    private static final Logger logger = LoggerFactory.getLogger(DealImportService.class);

    // 🕒 How to read dates like "2025-11-13T10:00:00"
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME; // Handles "2025-11-13T10:00:00"

    // 🔗 Connect to our storage managers
    private final DealRepository dealRepository;
    private final DealErrorRepository dealErrorRepository;

    @Autowired // 🎯 Spring automatically provides these
    public DealImportService(DealRepository dealRepository, DealErrorRepository dealErrorRepository) {
        this.dealRepository = dealRepository;
        this.dealErrorRepository = dealErrorRepository;
    }

    //🍳 Process the entire CSV file
    @Transactional // 🎯 This is a database operation
    public void importDealsFromCsv(String filePath) {
        logger.info("Starting CSV import from file: {}", filePath);

        Path path = Path.of(filePath);

        // 🚫 Check if file exists first
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

            // 📖 Read CSV line by line
            for (CSVRecord record : parser) {
                try {
                    processDealRecord(record);// 🍳 Cook this line(record)
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    logger.error("Failed to process record {}: {}", record.getRecordNumber(), e.getMessage());
                    // 🚨 Continue cooking other orders even if this one fails!
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
        // 📋 Get ingredients from CSV
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
            return;// 🚫 Stop if missing ingredients
        }

        // 🚀 Step 2: Check for duplicate deal
        if (dealRepository.existsByDealUniqueId(dealUniqueId)) {
            saveDealError(dealUniqueId, fromCurrency, toCurrency, timestampStr, amountStr,
                    "Duplicate deal ID");
            return;// 🚫 Stop if duplicate
        }

        // 🚀 Step 3: Check if timestamp is valid
        LocalDateTime dealTimestamp;
        try {
            dealTimestamp = LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
        } catch (DateTimeParseException e) {
            saveDealError(dealUniqueId, fromCurrency, toCurrency, timestampStr, amountStr,
                    "Invalid timestamp format. Expected: YYYY-MM-DDTHH:MM:SS");
            return;// 🚫 Stop if bad timestamp
        }

        // 🚀 Step 4: Check if amount is valid
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr.trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                saveDealError(dealUniqueId, fromCurrency, toCurrency, timestampStr, amountStr,
                        "Deal amount must be greater than 0");
                return; // 🚫 Stop if amount is zero or negative
            }
        } catch (NumberFormatException e) {
            saveDealError(dealUniqueId, fromCurrency, toCurrency, timestampStr, amountStr,
                    "Invalid amount format");
            return; // 🚫 Stop if amount is not a number
        }

        // 🚀 Step 5: Check currency codes
        if (fromCurrency.trim().length() != 3 || toCurrency.trim().length() != 3) {
            saveDealError(dealUniqueId, fromCurrency, toCurrency, timestampStr, amountStr,
                    "Currency codes must be exactly 3 characters");
            return; // 🚫 Stop if bad currency codes
        }

        // 🚀 Step 6: All checks passed! Save the deal
        try {
            Deal deal = new Deal(dealUniqueId, fromCurrency, toCurrency, dealTimestamp, amount);
            dealRepository.save(deal); // 💾 Save to database
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