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
public class Costume {
    private UUID id;
    private UUID categoryId;
    private String name;
    private String description;
    private String imagePath;
    private BigDecimal pricePerDay;
    private BigDecimal depositAmount;
}
