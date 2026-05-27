package com.oliinyk.costumes.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rental {
    private UUID id;
    private UUID userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPrice;
    private BigDecimal penaltyAmount;
    private String status;
}
