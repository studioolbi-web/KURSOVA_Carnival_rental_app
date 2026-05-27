package com.oliinyk.costumes.service;

import static org.junit.jupiter.api.Assertions.*;

import com.oliinyk.costumes.dto.RentalDTO;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReportServiceTest {

    @TempDir File tempDir;

    @Test
    void exportRentalsToCsv_Successful() throws IOException {
        ReportService reportService = new ReportService();
        File reportFile = new File(tempDir, "test_report.csv");

        RentalDTO rental =
                RentalDTO.builder()
                        .id(UUID.randomUUID())
                        .userEmail("user@example.com")
                        .startDate(LocalDate.of(2026, 5, 1))
                        .endDate(LocalDate.of(2026, 5, 5))
                        .status("COMPLETED")
                        .totalPrice(new BigDecimal("100.00"))
                        .totalDeposit(new BigDecimal("50.00"))
                        .penaltyAmount(new BigDecimal("10.00"))
                        .costumeNames(List.of("Costume 1", "Costume 2"))
                        .build();

        reportService.exportRentalsToCsv(List.of(rental), reportFile);

        assertTrue(reportFile.exists());
        List<String> lines = Files.readAllLines(reportFile.toPath());

        // Lines: BOM+Header, Data
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("Користувач"));
        assertTrue(lines.get(1).contains("user@example.com"));
        assertTrue(lines.get(1).contains("Costume 1; Costume 2"));
    }
}
