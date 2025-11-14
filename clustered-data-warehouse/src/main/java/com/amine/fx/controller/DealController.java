package com.amine.fx.controller;

import com.amine.fx.service.DealImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/deals")
public class DealController {

    @Autowired
    private DealImportService dealImportService;

    /**
     * 🎯 Health check endpoint - Test if API is working
     * GET http://localhost:8080/api/deals/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("✅ FX Data Warehouse is running!");
    }

    /**
     * 🎯 SUPER SIMPLE test endpoint
     * POST http://localhost:8080/api/deals/test-upload
     */
    @PostMapping("/test-upload")
    public ResponseEntity<?> testUpload(@RequestParam(value = "file", required = false) MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        if (file == null) {
            response.put("status", "error");
            response.put("message", "File is NULL - check Postman configuration");
            response.put("help", "Make sure: 1) Body is form-data 2) Key is 'file' 3) Type is 'File' not 'Text'");
            return ResponseEntity.badRequest().body(response);
        }

        response.put("status", "success");
        response.put("fileName", file.getOriginalFilename());
        response.put("fileSize", file.getSize());
        response.put("message", "File received successfully!");

        return ResponseEntity.ok(response);
    }

    /**
     * 🎯 Upload CSV file via API - WITH OPTIONAL PARAMETER
     * POST http://localhost:8080/api/deals/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadCsvFile(@RequestParam(value = "file", required = false) MultipartFile file) {
        System.out.println("📥 File upload request received");

        // 🎯 Check if file is null
        if (file == null) {
            System.out.println("❌ File parameter is null");

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "No file parameter received. Please check Postman configuration.");
            response.put("help", "Make sure: 1) Body is form-data 2) Key is 'file' 3) Type is 'File' not 'Text'");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 🎯 Debug: Print file info
            System.out.println("📋 File name: " + file.getOriginalFilename());
            System.out.println("📏 File size: " + file.getSize() + " bytes");
            System.out.println("📦 Content type: " + file.getContentType());

            // 🎯 Check if file is empty
            if (file.isEmpty()) {
                System.out.println("❌ File is empty");
                return ResponseEntity.badRequest().body("❌ Please select a CSV file (file is empty)");
            }

            // 🎯 Check file size
            if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
                System.out.println("❌ File too large: " + file.getSize() + " bytes");
                return ResponseEntity.badRequest().body("❌ File too large. Maximum size is 10MB");
            }

            // 🎯 Check if it's a CSV file
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                System.out.println("❌ File has no name");
                return ResponseEntity.badRequest().body("❌ File has no name");
            }

            if (!originalFilename.toLowerCase().endsWith(".csv")) {
                System.out.println("❌ Not a CSV file: " + originalFilename);
                return ResponseEntity.badRequest().body("❌ Please upload a CSV file. Received: " + originalFilename);
            }

            // 🎯 Create temp directory if it doesn't exist
            String tempDir = System.getProperty("java.io.tmpdir");
            System.out.println("📁 Temp directory: " + tempDir);

            Path tempFilePath = Path.of(tempDir, "uploaded_deals_" + System.currentTimeMillis() + ".csv");

            // 🎯 Save uploaded file temporarily
            Files.copy(file.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ File saved to: " + tempFilePath);

            // 🎯 Process the CSV file
            System.out.println("🔄 Starting CSV processing...");
            dealImportService.importDealsFromCsv(tempFilePath.toString());
            System.out.println("✅ CSV processing completed");

            // 🎯 Clean up temporary file
            Files.deleteIfExists(tempFilePath);
            System.out.println("🧹 Temporary file cleaned up");

            // 🎯 Return success response
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "CSV file processed successfully");
            response.put("filename", originalFilename);
            response.put("size", file.getSize() + " bytes");

            System.out.println("🎉 Upload completed successfully");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.err.println("❌ File processing error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ File processing error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    /**
     * 🎯 Import from fixed file path (for testing)
     * POST http://localhost:8080/api/deals/import-fixed
     */
    @PostMapping("/import-fixed")
    public ResponseEntity<?> importFixedFile() {
        try {
            String csvPath = "C:\\Users\\amine\\Desktop\\clustered-data-warehouse\\clustered-data-warehouse\\src\\main\\resources\\sample-deals.csv";  // Your file path
            dealImportService.importDealsFromCsv(csvPath);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Fixed file import completed");
            response.put("filePath", csvPath);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Import failed: " + e.getMessage());
        }
    }

    /**
     * 🎯 Simple file info endpoint for debugging
     * POST http://localhost:8080/api/deals/debug-upload
     */
    @PostMapping("/debug-upload")
    public ResponseEntity<?> debugUpload(@RequestParam(value = "file", required = false) MultipartFile file) {
        Map<String, Object> debugInfo = new HashMap<>();

        if (file == null) {
            debugInfo.put("status", "error");
            debugInfo.put("message", "No file received");
            return ResponseEntity.badRequest().body(debugInfo);
        }

        debugInfo.put("fileName", file.getOriginalFilename());
        debugInfo.put("fileSize", file.getSize());
        debugInfo.put("contentType", file.getContentType());
        debugInfo.put("isEmpty", file.isEmpty());
        debugInfo.put("tempDir", System.getProperty("java.io.tmpdir"));

        System.out.println("🔍 Debug Info: " + debugInfo);

        return ResponseEntity.ok(debugInfo);
    }
}