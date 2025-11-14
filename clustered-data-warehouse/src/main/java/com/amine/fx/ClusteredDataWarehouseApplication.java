package com.amine.fx;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.amine.fx.service.DealImportService;

@SpringBootApplication
public class ClusteredDataWarehouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClusteredDataWarehouseApplication.class, args);
    }

    @Bean
    CommandLineRunner runImporter(DealImportService dealImportService) {
        return args -> {
            String csvPath = "clustered-data-warehouse/src/main/resources/sample-deals.csv";
            System.out.println("🚀 Starting CSV import from: " + csvPath);
            dealImportService.importDealsFromCsv(csvPath); // 🚀 Changed to new method name
            System.out.println("✅ CSV import complete.");
        };
    }
}