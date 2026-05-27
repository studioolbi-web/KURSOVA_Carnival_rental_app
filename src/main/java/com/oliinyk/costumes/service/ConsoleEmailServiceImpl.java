package com.oliinyk.costumes.service;

/** Реалізація сервісу EmailService, яка виводить повідомлення в консоль (для тестування). */
public class ConsoleEmailServiceImpl implements EmailService {

    /**
     * "Відправляє" лист, виводячи його вміст у системну консоль.
     *
     * @param email Адреса отримувача
     * @param verificationCode Код підтвердження
     */
    @Override
    public void sendVerificationEmail(String email, String verificationCode) {
        // Симуляція відправки email через вивід у консоль
        System.out.println("=====================================");
        System.out.println("EMAIL ПОВІДОМЛЕННЯ");
        System.out.println("Кому: " + email);
        System.out.println("Тема: Підтвердження реєстрації");
        System.out.println("Код підтвердження: " + verificationCode);
        System.out.println("=====================================");
    }
}
