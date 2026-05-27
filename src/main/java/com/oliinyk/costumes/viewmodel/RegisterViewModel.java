package com.oliinyk.costumes.viewmodel;

import com.oliinyk.costumes.service.AuthService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel для вікна реєстрації та верифікації користувачів. Керує станом реєстраційної форми та
 * взаємодіє з сервісом аутентифікації.
 */
public class RegisterViewModel {
    private final AuthService authService;

    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final StringProperty code = new SimpleStringProperty("");
    private final StringProperty errorMessage = new SimpleStringProperty("");
    private final BooleanProperty registrationSuccessful = new SimpleBooleanProperty(false);
    private final BooleanProperty verificationSuccessful = new SimpleBooleanProperty(false);

    /**
     * Конструктор ViewModel для реєстрації.
     *
     * @param authService сервіс аутентифікації для реєстрації та верифікації
     */
    public RegisterViewModel(AuthService authService) {
        this.authService = authService;
    }

    /**
     * @return властивість для поля електронної пошти
     */
    public StringProperty emailProperty() {
        return email;
    }

    /**
     * @return властивість для поля пароля
     */
    public StringProperty passwordProperty() {
        return password;
    }

    /**
     * @return властивість для поля коду верифікації
     */
    public StringProperty codeProperty() {
        return code;
    }

    /**
     * @return властивість для повідомлення про помилку
     */
    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    /**
     * @return властивість успішності реєстрації
     */
    public BooleanProperty registrationSuccessfulProperty() {
        return registrationSuccessful;
    }

    /**
     * @return властивість успішності верифікації
     */
    public BooleanProperty verificationSuccessfulProperty() {
        return verificationSuccessful;
    }

    /** Спроба зареєструвати нового користувача. Валідує дані та викликає сервіс реєстрації. */
    public void attemptRegister() {
        if (email.get() == null
                || email.get().trim().isEmpty()
                || password.get() == null
                || password.get().isEmpty()) {
            errorMessage.set("Введіть email та пароль.");
            return;
        }

        try {
            authService.registerUser(email.get(), password.get(), "USER");
            errorMessage.set("");
            registrationSuccessful.set(true);
        } catch (IllegalArgumentException e) {
            errorMessage.set(e.getMessage());
        } catch (Exception e) {
            errorMessage.set("Помилка реєстрації: " + e.getMessage());
        }
    }

    /** Спроба верифікувати акаунт за допомогою коду. */
    public void attemptVerify() {
        if (authService.verifyUser(email.get(), code.get())) {
            errorMessage.set("Успішно! Тепер увійдіть.");
            verificationSuccessful.set(true);
        } else {
            errorMessage.set("Невірний код.");
        }
    }
}
