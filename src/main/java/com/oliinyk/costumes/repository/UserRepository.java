package com.oliinyk.costumes.repository;

import com.oliinyk.costumes.model.User;
import java.util.Optional;

/**
 * Репозиторій для управління даними користувачів. Розширює базовий інтерфейс Repository для моделі
 * User.
 */
public interface UserRepository extends Repository<User> {
    /**
     * Знаходить користувача за його електронною поштою.
     *
     * @param email електронна пошта користувача для пошуку
     * @return Optional, що містить знайденого користувача, або порожній Optional, якщо користувача
     *     не знайдено
     */
    Optional<User> findByEmail(String email);
}
