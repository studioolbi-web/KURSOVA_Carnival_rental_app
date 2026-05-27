package com.oliinyk.costumes.service;

import com.oliinyk.costumes.model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** Менеджер сесії користувача та глобальних налаштувань додатку. Реалізує патерн Singleton. */
public class SessionManager {
    private User currentUser;

    private final StringProperty viewMode = new SimpleStringProperty("GRID");
    private final StringProperty theme = new SimpleStringProperty("LIGHT");

    private SessionManager() {}

    private static class Holder {
        private static final SessionManager INSTANCE = new SessionManager();
    }

    /**
     * Отримати єдиний екземпляр менеджера сесії (Singleton).
     *
     * @return Екземпляр SessionManager
     */
    public static SessionManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Встановити поточного користувача після успішного входу.
     *
     * @param user Об'єкт авторизованого користувача
     */
    public void login(User user) {
        this.currentUser = user;
    }

    /** Очистити дані поточної сесії (вихід із системи). */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Отримати дані поточного авторизованого користувача.
     *
     * @return Об'єкт User або null, якщо ніхто не увійшов
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Перевірити, чи є активна сесія користувача.
     *
     * @return true, якщо користувач авторизований, інакше false
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Перевірити, чи має поточний користувач права адміністратора.
     *
     * @return true, якщо користувач адміністратор, інакше false
     */
    public boolean isAdmin() {
        return isLoggedIn() && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    /**
     * Властивість режиму відображення каталогу (GRID або LIST). Використовується для зв'язування з
     * інтерфейсом JavaFX.
     *
     * @return Властивість viewMode
     */
    public StringProperty viewModeProperty() {
        return viewMode;
    }

    /**
     * Властивість поточної теми додатку (LIGHT або DARK). Використовується для зв'язування з
     * інтерфейсом JavaFX.
     *
     * @return Властивість theme
     */
    public StringProperty themeProperty() {
        return theme;
    }
}
