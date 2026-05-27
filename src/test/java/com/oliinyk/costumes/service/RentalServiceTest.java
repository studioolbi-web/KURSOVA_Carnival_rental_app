package com.oliinyk.costumes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.oliinyk.costumes.model.Costume;
import com.oliinyk.costumes.model.Rental;
import com.oliinyk.costumes.repository.RentalItemRepository;
import com.oliinyk.costumes.repository.RentalRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {

    @Mock private RentalRepository rentalRepository;

    @Mock private RentalItemRepository rentalItemRepository;

    private RentalService rentalService;
    private Costume testCostume;

    @BeforeEach
    void setUp() {
        rentalService = new RentalService(rentalRepository, rentalItemRepository);
        testCostume =
                Costume.builder()
                        .id(UUID.randomUUID())
                        .name("Тестовий костюм")
                        .pricePerDay(new BigDecimal("100.00"))
                        .depositAmount(new BigDecimal("200.00"))
                        .build();
    }

    @Test
    void calculatePenalty_ShouldReturnZero_WhenNotOverdue() {
        Rental rental =
                Rental.builder().endDate(LocalDate.now().plusDays(1)).status("ACTIVE").build();
        List<Costume> costumes = List.of(testCostume);

        BigDecimal penalty = rentalService.calculatePenalty(rental, costumes);

        assertEquals(0, penalty.compareTo(BigDecimal.ZERO));
    }

    @Test
    void calculatePenalty_ShouldReturnCorrectAmount_WhenOverdue() {
        Rental rental =
                Rental.builder().endDate(LocalDate.now().minusDays(2)).status("ACTIVE").build();
        // 2 дні прострочки * 100.00 грн/день = 200.00 грн
        List<Costume> costumes = List.of(testCostume);

        BigDecimal penalty = rentalService.calculatePenalty(rental, costumes);

        assertEquals(0, new BigDecimal("200.00").compareTo(penalty));
    }
}
