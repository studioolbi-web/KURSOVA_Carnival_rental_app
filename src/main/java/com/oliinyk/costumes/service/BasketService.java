package com.oliinyk.costumes.service;

import com.oliinyk.costumes.model.Costume;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Сервіс для керування кошиком обраних костюмів. Реалізує патерн Singleton. */
public class BasketService {
    private final List<Costume> items = new ArrayList<>();

    private BasketService() {}

    private static class Holder {
        private static final BasketService INSTANCE = new BasketService();
    }

    /**
     * Отримати єдиний екземпляр сервісу (Singleton).
     *
     * @return Екземпляр BasketService
     */
    public static BasketService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Додати костюм до кошика.
     *
     * @param costume Костюм, який потрібно додати
     */
    public void addItem(Costume costume) {
        items.add(costume);
    }

    /**
     * Видалити костюм з кошика.
     *
     * @param costume Костюм, який потрібно видалити
     */
    public void removeItem(Costume costume) {
        items.remove(costume);
    }

    /**
     * Отримати список усіх костюмів у кошику.
     *
     * @return Список костюмів
     */
    public List<Costume> getItems() {
        return new ArrayList<>(items);
    }

    /** Очистити кошик від усіх елементів. */
    public void clear() {
        items.clear();
    }

    /**
     * Розрахувати загальну вартість оренди для всіх елементів у кошику. Враховує кількість днів та
     * можливі знижки за тривалість оренди.
     *
     * @param days Кількість днів оренди
     * @return Загальна вартість оренди
     */
    public BigDecimal calculateRentalTotal(long days) {
        BigDecimal total = BigDecimal.ZERO;
        for (Costume item : items) {
            total = total.add(item.getPricePerDay());
        }
        total = total.multiply(BigDecimal.valueOf(days > 0 ? days : 1));

        // Логіка лояльності: знижка 10% при оренді від 3 днів (Блок 2)
        if (days >= 3) {
            total = total.multiply(new BigDecimal("0.90"));
        }
        return total;
    }

    /**
     * Розрахувати загальну суму застави для всіх костюмів у кошику.
     *
     * @return Загальна сума застави
     */
    public BigDecimal calculateTotalDeposit() {
        BigDecimal total = BigDecimal.ZERO;
        for (Costume item : items) {
            total =
                    total.add(
                            item.getDepositAmount() != null
                                    ? item.getDepositAmount()
                                    : BigDecimal.ZERO);
        }
        return total;
    }

    /**
     * Розрахувати суму знижки залежно від кількості днів оренди.
     *
     * @param days Кількість днів оренди
     * @return Сума знижки
     */
    public BigDecimal calculateDiscount(long days) {
        if (days < 3) return BigDecimal.ZERO;

        BigDecimal totalWithoutDiscount = BigDecimal.ZERO;
        for (Costume item : items) {
            totalWithoutDiscount = totalWithoutDiscount.add(item.getPricePerDay());
        }
        totalWithoutDiscount = totalWithoutDiscount.multiply(BigDecimal.valueOf(days));

        return totalWithoutDiscount.multiply(new BigDecimal("0.10"));
    }
}
