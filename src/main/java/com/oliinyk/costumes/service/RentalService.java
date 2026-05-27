package com.oliinyk.costumes.service;

import com.oliinyk.costumes.infrastructure.DatabaseManager;
import com.oliinyk.costumes.model.Costume;
import com.oliinyk.costumes.model.Rental;
import com.oliinyk.costumes.model.RentalItem;
import com.oliinyk.costumes.model.User;
import com.oliinyk.costumes.repository.RentalItemRepository;
import com.oliinyk.costumes.repository.RentalRepository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/** Сервіс для управління процесом оренди костюмів. Забезпечує бізнес-логіку та транзакційність. */
public class RentalService {

    private final RentalRepository rentalRepository;
    private final RentalItemRepository rentalItemRepository;

    /**
     * Конструктор для створення екземпляра сервісу оренди.
     *
     * @param rentalRepository Репозиторій для роботи з орендами
     * @param rentalItemRepository Репозиторій для роботи з елементами оренди
     */
    public RentalService(
            RentalRepository rentalRepository, RentalItemRepository rentalItemRepository) {
        this.rentalRepository = rentalRepository;
        this.rentalItemRepository = rentalItemRepository;
    }

    /**
     * Оформлення нового замовлення (Checkout). Використовує транзакцію для збереження інформації
     * про оренду та її елементи.
     *
     * @param user Користувач, який оформлює замовлення
     * @param costumes Список костюмів для оренди
     * @param startDate Дата початку оренди
     * @param endDate Дата закінчення оренди
     * @return Об'єкт створеної оренди
     * @throws IllegalArgumentException якщо дані некоректні або кошик порожній
     * @throws IllegalStateException якщо один з костюмів недоступний на вказані дати
     * @throws RuntimeException при помилках роботи з базою даних
     */
    public Rental checkout(
            User user, List<Costume> costumes, LocalDate startDate, LocalDate endDate) {
        // 1. Бізнес-валідація
        validateRentalDates(startDate, endDate);
        if (costumes == null || costumes.isEmpty()) {
            throw new IllegalArgumentException("Кошик порожній.");
        }

        // Перевірка доступності кожного костюма
        for (Costume costume : costumes) {
            if (!rentalRepository.isCostumeAvailable(costume.getId(), startDate, endDate)) {
                throw new IllegalStateException(
                        "Костюм '" + costume.getName() + "' вже орендований на ці дати.");
            }
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days == 0) days = 1;

        // Використання нової логіки розрахунку з BasketService (Блок 2)
        BasketService basketService = BasketService.getInstance();
        BigDecimal rentalPrice = basketService.calculateRentalTotal(days);
        BigDecimal totalDeposit = basketService.calculateTotalDeposit();

        // Загальна сума до сплати при оформленні
        BigDecimal finalPrice = rentalPrice.add(totalDeposit);

        Rental rental =
                Rental.builder()
                        .id(UUID.randomUUID())
                        .userId(user.getId())
                        .startDate(startDate)
                        .endDate(endDate)
                        .totalPrice(finalPrice)
                        .penaltyAmount(BigDecimal.ZERO)
                        .status("RESERVED") // Початковий статус за новою схемою
                        .build();

        // 2. Виконання транзакції (Вимога розділу 3.4 - чистий JDBC)
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Початок транзакції
            try {
                // Використання репозиторіїв у межах однієї транзакції
                rentalRepository.save(rental, conn);

                for (Costume costume : costumes) {
                    RentalItem item =
                            RentalItem.builder()
                                    .rentalId(rental.getId())
                                    .costumeId(costume.getId())
                                    .priceAtRental(costume.getPricePerDay())
                                    .build();
                    rentalItemRepository.save(item, conn);
                }

                conn.commit(); // Підтвердження транзакції
                return rental;
            } catch (Exception e) {
                conn.rollback(); // Відкат транзакції у разі помилки
                throw new RuntimeException(
                        "Помилка при оформленні оренди. Тразакцію скасовано.", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка з'єднання з базою даних", e);
        }
    }

    /**
     * Розрахувати штраф за прострочення оренди.
     *
     * @param rental Об'єкт оренди
     * @param costumes Список костюмів у цій оренді
     * @return Сума нарахованого штрафу
     */
    public BigDecimal calculatePenalty(Rental rental, List<Costume> costumes) {
        if (rental.getEndDate().isAfter(LocalDate.now())) {
            return BigDecimal.ZERO;
        }

        long overdueDays = ChronoUnit.DAYS.between(rental.getEndDate(), LocalDate.now());
        if (overdueDays <= 0) return BigDecimal.ZERO;

        BigDecimal dailyRate = BigDecimal.ZERO;
        for (Costume c : costumes) {
            dailyRate = dailyRate.add(c.getPricePerDay());
        }

        return dailyRate.multiply(BigDecimal.valueOf(overdueDays));
    }

    /**
     * Оновлення статусу оренди з автоматичною перевіркою прострочки та нарахуванням штрафу.
     *
     * @param rental Об'єкт оренди для оновлення
     * @param newStatus Новий статус ("RETURNED", "CANCELLED" тощо)
     * @param costumes Список костюмів, пов'язаних з орендою
     */
    public void updateRentalStatus(Rental rental, String newStatus, List<Costume> costumes) {
        if ("RETURNED".equals(newStatus)) {
            BigDecimal penalty = calculatePenalty(rental, costumes);
            rental.setPenaltyAmount(penalty);
        }

        // Автоматичне переведення в OVERDUE, якщо дата пройшла і статус не RETURNED
        if (!"RETURNED".equals(newStatus) && rental.getEndDate().isBefore(LocalDate.now())) {
            newStatus = "OVERDUE";
        }

        rental.setStatus(newStatus);
        rentalRepository.update(rental);
    }

    /**
     * Видаляє оренду та всі її позиції з бази даних.
     *
     * @param rentalId Унікальний ідентифікатор оренди
     */
    public void deleteRental(UUID rentalId) {
        rentalRepository.delete(rentalId);
    }

    private void validateRentalDates(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Дати не можуть бути порожніми.");
        }
        if (start.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Дата початку не може бути в минулому.");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException(
                    "Дата початку не може бути пізніше дати завершення.");
        }
    }
}
