package com.oliinyk.costumes.viewmodel;

import com.oliinyk.costumes.service.SessionManager;
import javafx.beans.property.StringProperty;

/** ViewModel для налаштувань. Керує станом теми та режимом відображення. */
public class SettingsViewModel {

    private final SessionManager sessionManager = SessionManager.getInstance();

    /**
     * @return властивість поточної теми (DARK/LIGHT)
     */
    public StringProperty themeProperty() {
        return sessionManager.themeProperty();
    }

    /**
     * @return властивість режиму відображення (GRID/LIST)
     */
    public StringProperty viewModeProperty() {
        return sessionManager.viewModeProperty();
    }

    /**
     * @return true, якщо вибрано темну тему
     */
    public boolean isDarkTheme() {
        return "DARK".equals(sessionManager.themeProperty().get());
    }

    /**
     * Встановлює тему оформлення.
     *
     * @param isDark true для темної теми, false для світлої
     */
    public void setTheme(boolean isDark) {
        sessionManager.themeProperty().set(isDark ? "DARK" : "LIGHT");
    }

    /**
     * @return true, якщо вибрано режим сітки (GRID)
     */
    public boolean isGridView() {
        return !"LIST".equals(sessionManager.viewModeProperty().get());
    }

    /**
     * Встановлює режим відображення каталогу.
     *
     * @param isGrid true для режиму сітки, false для списку
     */
    public void setViewMode(boolean isGrid) {
        sessionManager.viewModeProperty().set(isGrid ? "GRID" : "LIST");
    }
}
