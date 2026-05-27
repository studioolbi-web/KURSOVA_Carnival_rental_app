package com.oliinyk.costumes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * DTO для передачі даних про замовлення (оренду). Використовується для відокремлення моделей БД від
 * шару представлення.
 */
@Data
@Builder
public class RentalDTO {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPrice;
    private BigDecimal penaltyAmount;
    private BigDecimal totalDeposit;
    private String status;
    private List<String> costumeNames;
    private List<String> costumeImages;
}
