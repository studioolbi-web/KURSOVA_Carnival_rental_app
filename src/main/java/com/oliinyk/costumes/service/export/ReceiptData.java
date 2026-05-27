package com.oliinyk.costumes.service.export;

import com.oliinyk.costumes.model.Costume;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Дані для експорту чеку.
 */
public class ReceiptData {
    public List<Costume> items;
    public LocalDate start;
    public LocalDate end;
    public long days;
    public BigDecimal rentalTotal;
    public BigDecimal deposit;
    public BigDecimal discount;
    public BigDecimal grandTotal;

    public ReceiptData(List<Costume> items, LocalDate start, LocalDate end, long days, BigDecimal rentalTotal, BigDecimal deposit, BigDecimal discount, BigDecimal grandTotal) {
        this.items = items;
        this.start = start;
        this.end = end;
        this.days = days;
        this.rentalTotal = rentalTotal;
        this.deposit = deposit;
        this.discount = discount;
        this.grandTotal = grandTotal;
    }
}
