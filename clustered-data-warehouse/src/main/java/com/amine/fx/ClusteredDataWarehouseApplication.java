package com.amine.fx;

import com.amine.fx.service.DealImportService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;

@SpringBootApplication
public class ClusteredDataWarehouseApplication {

    public static void main(String[] args) {
        // Start Spring Boot context
        var context = SpringApplication.run(ClusteredDataWarehouseApplication.class, args);

        // Get the DealImportService bean from Spring context
        DealImportService service = context.getBean(DealImportService.class);

        // Path to CSV file (relative to project root)
        Path csvPath = Path.of("src/main/resources/sample-deals.csv");

        System.out.println("📁 Importing CSV from: " + csvPath.toAbsolutePath());

        // Trigger CSV import
        service.importCsv(csvPath);

        System.out.println("✅ CSV import finished!");

        // Exit Spring Boot gracefully
        SpringApplication.exit(context, () -> 0);
    }
}
