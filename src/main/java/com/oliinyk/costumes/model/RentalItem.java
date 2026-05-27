package com.oliinyk.costumes.model;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalItem {
    private UUID rentalId;
    private UUID costumeId;
    private BigDecimal priceAtRental;
}
