package com.oliinyk.costumes.viewmodel;

import com.oliinyk.costumes.model.Costume;
import com.oliinyk.costumes.service.BasketService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel для каталогу костюмів. Відповідає за відображення списку костюмів, фільтрацію та
 * управління кошиком.
 */
public class CatalogViewModel {

    private final ObservableList<Costume> costumes = FXCollections.observableArrayList();
    private final BooleanProperty isDarkTheme = new SimpleBooleanProperty(false);
    private final StringProperty cartButtonText = new SimpleStringProperty("Кошик (0)");
    private int cartCount = 0;

    /** Конструктор ViewModel каталогу. Автоматично завантажує дані з бази. */
    public CatalogViewModel() {
        loadFromDatabase();
    }

    /**
     * Повертає список костюмів для відображення.
     *
     * @return ObservableList костюмів
     */
    public ObservableList<Costume> getCostumes() {
        return costumes;
    }

    /**
     * Повертає властивість поточної теми (темна/світла).
     *
     * @return BooleanProperty теми
     */
    public BooleanProperty isDarkThemeProperty() {
        return isDarkTheme;
    }

    /** Перемикає тему оформлення. */
    public void toggleTheme() {
        isDarkTheme.set(!isDarkTheme.get());
    }

    /**
     * Повертає властивість тексту кнопки кошика.
     *
     * @return StringProperty тексту кошика
     */
    public StringProperty cartButtonTextProperty() {
        return cartButtonText;
    }

    /**
     * Додає костюм до кошика.
     *
     * @param costume об'єкт костюма для додавання
     */
    public void addToCart(Costume costume) {
        BasketService.getInstance().addItem(costume);
        cartCount++;
        cartButtonText.set("Кошик (" + cartCount + ")");
    }

    /** Завантажує список костюмів з репозиторію. */
    public void loadFromDatabase() {
        com.oliinyk.costumes.repository.CostumeRepository repo =
                new com.oliinyk.costumes.repository.JdbcCostumeRepository();
        costumes.setAll(repo.findAll());
    }
}
