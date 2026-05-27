package com.oliinyk.costumes.service;

import static org.junit.jupiter.api.Assertions.*;

import com.oliinyk.costumes.model.Costume;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Тести для BasketService. Перевіряють розрахунок вартості та знижок (Вимога розділу 7.1). */
public class BasketServiceTest {

    private BasketService basketService;
    private Costume testCostume;

    @BeforeEach
    void setUp() {
        basketService = BasketService.getInstance();
        basketService.clear();
        testCostume =
                Costume.builder()
                        .pricePerDay(new BigDecimal("100.00"))
                        .depositAmount(new BigDecimal("200.00"))
                        .build();
    }

    @Test
    void calculateRentalTotal_ShouldApplyDiscount_WhenThreeOrMoreDays() {
        basketService.addItem(testCostume);

        // 3 дні * 100.00 грн = 300.00 грн. Знижка 10% = 270.00 грн
        BigDecimal total = basketService.calculateRentalTotal(3);

        assertEquals(0, new BigDecimal("270.00").compareTo(total));
    }

    @Test
    void calculateRentalTotal_ShouldNotApplyDiscount_WhenLessThanThreeDays() {
        basketService.addItem(testCostume);

        // 2 дні * 100.00 грн = 200.00 грн
        BigDecimal total = basketService.calculateRentalTotal(2);

        assertEquals(0, new BigDecimal("200.00").compareTo(total));
    }

    @Test
    void calculateTotalDeposit_ShouldSumAllItems() {
        basketService.addItem(testCostume);
        basketService.addItem(testCostume);

        BigDecimal deposit = basketService.calculateTotalDeposit();

        assertEquals(0, new BigDecimal("400.00").compareTo(deposit));
    }

    @Test
    void calculateDiscount_ShouldReturnCorrectAmount() {
        basketService.addItem(testCostume);

        // 3 дні * 100.00 грн = 300.00 грн. Знижка 10% = 30.00 грн
        BigDecimal discount = basketService.calculateDiscount(3);

        assertEquals(0, new BigDecimal("30.00").compareTo(discount));
    }
}
