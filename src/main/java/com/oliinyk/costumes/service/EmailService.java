package com.oliinyk.costumes.service;

/** Інтерфейс сервісу для відправки електронної пошти. */
public interface EmailService {
    /**
     * Відправити лист для верифікації електронної адреси користувача.
     *
     * @param email Адреса отримувача
     * @param verificationCode Код підтвердження
     */
    void sendVerificationEmail(String email, String verificationCode);
}
