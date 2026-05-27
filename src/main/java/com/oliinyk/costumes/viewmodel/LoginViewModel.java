package com.oliinyk.costumes.viewmodel;

import com.oliinyk.costumes.service.AuthService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel для вікна входу в систему. Забезпечує зв'язок між інтерфейсом користувача та сервісом
 * аутентифікації.
 */
public class LoginViewModel {

    private final AuthService authService;

    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final StringProperty errorMessage = new SimpleStringProperty("");
    private final BooleanProperty loginSuccessful = new SimpleBooleanProperty(false);

    /**
     * Конструктор ViewModel для входу.
     *
     * @param authService сервіс аутентифікації для перевірки даних
     */
    public LoginViewModel(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Повертає властивість для поля електронної пошти.
     *
     * @return StringProperty для email
     */
    public StringProperty emailProperty() {
        return email;
    }

    /**
     * Повертає властивість для поля пароля.
     *
     * @return StringProperty для пароля
     */
    public StringProperty passwordProperty() {
        return password;
    }

    /**
     * Повертає властивість для повідомлення про помилку.
     *
     * @return StringProperty для тексту помилки
     */
    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    /**
     * Повертає властивість, що вказує на успішність входу.
     *
     * @return BooleanProperty успішності входу
     */
    public BooleanProperty loginSuccessfulProperty() {
        return loginSuccessful;
    }

    /**
     * Здійснює спробу входу в систему. Перевіряє введені дані та оновлює статус входу або
     * повідомлення про помилку.
     */
    public void attemptLogin() {
        if (email.get() == null || email.get().isEmpty()) {
            errorMessage.set("Email не може бути порожнім");
            return;
        }

        authService
                .login(email.get(), password.get())
                .ifPresentOrElse(
                        user -> {
                            if (!user.isVerified()) {
                                errorMessage.set("Акаунт не верифіковано.");
                                loginSuccessful.set(false);
                            } else {
                                com.oliinyk.costumes.service.SessionManager.getInstance()
                                        .login(user);
                                errorMessage.set("");
                                loginSuccessful.set(true);
                            }
                        },
                        () -> errorMessage.set("Неправильний email або пароль"));
    }
}
