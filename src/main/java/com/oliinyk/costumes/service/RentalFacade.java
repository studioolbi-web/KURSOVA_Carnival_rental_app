package com.oliinyk.costumes.service;

import com.oliinyk.costumes.dto.RentalDTO;
import com.oliinyk.costumes.model.Costume;
import com.oliinyk.costumes.model.Rental;
import com.oliinyk.costumes.model.User;
import com.oliinyk.costumes.repository.CostumeRepository;
import com.oliinyk.costumes.repository.RentalRepository;
import com.oliinyk.costumes.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Фасад для роботи з орендами. Реалізує патерн Facade (Вимога розділу 4.3.2). Спрощує доступ до
 * бізнес-логіки для шару представлення.
 */
public class RentalFacade {

    private final RentalService rentalService;
    private final RentalRepository rentalRepository;
    private final CostumeRepository costumeRepository;
    private final UserRepository userRepository;

    /**
     * Конструктор фасаду.
     *
     * @param rentalService Сервіс оренди
     * @param rentalRepository Репозиторій оренд
     * @param costumeRepository Репозиторій костюмів
     * @param userRepository Репозиторій користувачів
     */
    public RentalFacade(
            RentalService rentalService,
            RentalRepository rentalRepository,
            CostumeRepository costumeRepository,
            UserRepository userRepository) {
        this.rentalService = rentalService;
        this.rentalRepository = rentalRepository;
        this.costumeRepository = costumeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Оформити замовлення та повернути об'єкт передачі даних (DTO).
     *
     * @param user Користувач
     * @param costumes Список костюмів
     * @param start Дата початку
     * @param end Дата завершення
     * @return Об'єкт RentalDTO з повною інформацією про оренду
     */
    public RentalDTO checkout(User user, List<Costume> costumes, LocalDate start, LocalDate end) {
        Rental rental = rentalService.checkout(user, costumes, start, end);
        return mapToDTO(rental, costumes);
    }

    /**
     * Оновити статус оренди за її ідентифікатором.
     *
     * @param rentalId Унікальний ідентифікатор оренди
     * @param newStatus Новий статус для встановлення
     */
    public void updateStatus(UUID rentalId, String newStatus) {
        rentalRepository
                .findById(rentalId)
                .ifPresent(
                        rental -> {
                            List<Costume> costumes = getCostumesByRentalId(rentalId);
                            rentalService.updateRentalStatus(rental, newStatus, costumes);
                        });
    }

    /**
     * Видаляє оренду за її ідентифікатором.
     *
     * @param rentalId Унікальний ідентифікатор оренди
     */
    public void deleteRental(UUID rentalId) {
        rentalService.deleteRental(rentalId);
    }

    /**
     * Отримати список костюмів, що входять до конкретної оренди.
     *
     * @param rentalId Унікальний ідентифікатор оренди
     * @return Список об'єктів Costume
     */
    public List<Costume> getCostumesByRentalId(UUID rentalId) {
        return new com.oliinyk.costumes.repository.JdbcRentalItemRepository()
                .findByRentalId(rentalId).stream()
                        .map(item -> costumeRepository.findById(item.getCostumeId()).orElse(null))
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toList());
    }

    /**
     * Отримати всі оренди у системі у форматі DTO.
     *
     * @return Список усіх RentalDTO
     */
    public List<RentalDTO> getAllRentals() {
        return rentalRepository.findAll().stream()
                .map(r -> mapToDTO(r, getCostumesByRentalId(r.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Отримати список оренд для конкретного користувача.
     *
     * @param userId Унікальний ідентифікатор користувача
     * @return Список RentalDTO для вказаного користувача
     */
    public List<RentalDTO> getRentalsByUserId(java.util.UUID userId) {
        return rentalRepository.findByUserId(userId).stream()
                .map(r -> mapToDTO(r, getCostumesByRentalId(r.getId())))
                .collect(Collectors.toList());
    }

    private RentalDTO mapToDTO(Rental rental, List<Costume> costumes) {
        String userEmail =
                userRepository.findById(rental.getUserId()).map(User::getEmail).orElse("Unknown");

        BigDecimal currentPenalty = rental.getPenaltyAmount();
        if (currentPenalty.compareTo(BigDecimal.ZERO) == 0
                && !"RETURNED".equals(rental.getStatus())) {
            currentPenalty = rentalService.calculatePenalty(rental, costumes);
        }

        BigDecimal totalDeposit = BigDecimal.ZERO;
        for (Costume c : costumes) {
            totalDeposit =
                    totalDeposit.add(
                            c.getDepositAmount() != null ? c.getDepositAmount() : BigDecimal.ZERO);
        }

        List<String> names = costumes.stream().map(Costume::getName).collect(Collectors.toList());
        List<String> images = costumes.stream().map(Costume::getImagePath).collect(Collectors.toList());

        return RentalDTO.builder()
                .id(rental.getId())
                .userId(rental.getUserId())
                .userEmail(userEmail)
                .startDate(rental.getStartDate())
                .endDate(rental.getEndDate())
                .totalPrice(rental.getTotalPrice())
                .penaltyAmount(currentPenalty)
                .totalDeposit(totalDeposit)
                .status(rental.getStatus())
                .costumeNames(names)
                .costumeImages(images)
                .build();
    }
}
