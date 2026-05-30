package com.oliinyk.costumes.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomLook {
    private UUID id;
    private UUID userId;
    private String name;
    private String imagePath;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    
    // Елементи (костюми/аксесуари), з яких складається образ
    private List<Costume> items;
}
