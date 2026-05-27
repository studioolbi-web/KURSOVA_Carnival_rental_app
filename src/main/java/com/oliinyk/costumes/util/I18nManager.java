package com.oliinyk.costumes.util;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Утиліта для динамічної зміни мови (i18n).
 */
public class I18nManager {
    private static final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(Locale.of("uk", "UA"));

    public static ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    public static Locale getLocale() {
        return locale.get();
    }

    public static void setLocale(Locale l) {
        locale.set(l);
    }

    public static String get(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle("bundles.messages", getLocale());
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key; // Якщо ключ не знайдено
        }
    }

    public static StringBinding createStringBinding(String key) {
        return javafx.beans.binding.Bindings.createStringBinding(() -> get(key), locale);
    }
}
