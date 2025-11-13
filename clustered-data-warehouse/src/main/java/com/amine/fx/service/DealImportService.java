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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DealImportService {

    private static final Logger logger = LoggerFactory.getLogger(DealImportService.class);

    private final DealRepository dealRepository;
    private final DealErrorRepository dealErrorRepository;

    public DealImportService(DealRepository dealRepository, DealErrorRepository dealErrorRepository) {
        this.dealRepository = dealRepository;
        this.dealErrorRepository = dealErrorRepository;
    }

    public void importCsv(Path csvPath) {
        logger.info("📁 Importing CSV from: {}", csvPath.toAbsolutePath());

        try (CSVParser parser = CSVParser.parse(csvPath,
                java.nio.charset.StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {

            for (CSVRecord record : parser) {
                String dealId = record.get("Deal Unique Id");
                String fromCurrency = record.get("From Currency ISO Code");
                String toCurrency = record.get("To Currency ISO Code");
                String timestampStr = record.get("Deal timestamp");
                String amountStr = record.get("Deal Amount");

                // Validate required fields
                if (dealId.isEmpty() || fromCurrency.isEmpty() || toCurrency.isEmpty() ||
                        timestampStr.isEmpty() || amountStr.isEmpty()) {

                    saveDealError(dealId.isEmpty() ? null : dealId, record.toString(),
                            "Missing required fields", timestampStr);
                    logger.error("❌ Missing required fields in row: {}", dealId.isEmpty() ? "(unknown id)" : dealId);
                    continue;
                }

                // Parse CSV fields
                LocalDateTime dealTimestamp;
                BigDecimal dealAmount;
                try {
                    dealTimestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    dealAmount = new BigDecimal(amountStr);
                } catch (Exception e) {
                    saveDealError(dealId, record.toString(), "Invalid timestamp or amount format", timestampStr);
                    logger.error("❌ Invalid format for row {}: {}", dealId, e.getMessage());
                    continue;
                }

                // Duplicate check in DB
                if (dealRepository.existsById(dealId)) {
                    // Only save error if this deal+timestamp combination does not exist in fx_deal_errors
                    if (!dealErrorRepository.existsByDealUniqueIdAndOccurredAt(dealId, dealTimestamp)) {
                        saveDealError(dealId, record.toString(), "Duplicate deal_unique_id", timestampStr);
                    }
                    logger.warn("⚠️ Duplicate skipped: {}", dealId);
                    continue;
                }

                // Save valid deal
                Deal deal = new Deal(dealId, fromCurrency, toCurrency, dealTimestamp, dealAmount);
                dealRepository.save(deal);
                logger.info("✅ Imported deal: {} | {} -> {} | Amount: {} | Timestamp: {}",
                        dealId, fromCurrency, toCurrency, dealAmount, dealTimestamp);
            }

            logger.info("✅ CSV import finished!");

        } catch (IOException e) {
            logger.error("❌ Failed to read CSV file: {}", e.getMessage());
        }
    }

    private void saveDealError(String dealUniqueId, String rawPayload, String reason, String timestampStr) {
        try {
            LocalDateTime occurredAt = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            DealError err = new DealError(
                    (dealUniqueId == null || dealUniqueId.isEmpty()) ? null : dealUniqueId,
                    rawPayload,
                    reason,
                    occurredAt
            );
            dealErrorRepository.save(err);
            logger.info("📝 Error persisted for {} (reason: {})", dealUniqueId == null ? "(unknown id)" : dealUniqueId, reason);
        } catch (Exception e) {
            logger.error("❌ Failed to persist DealError for {}: {}", dealUniqueId, e.getMessage());
        }
    }
}
