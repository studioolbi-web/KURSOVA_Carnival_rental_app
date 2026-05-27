package com.oliinyk.costumes.viewmodel;

import com.oliinyk.costumes.model.Costume;
import com.oliinyk.costumes.model.User;
import com.oliinyk.costumes.service.BasketService;
import com.oliinyk.costumes.service.RentalFacade;
import com.oliinyk.costumes.service.SessionManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/** ViewModel для кошика. Інкапсулює бізнес-логіку та стан для BasketController. */
public class BasketViewModel {

    private final RentalFacade rentalFacade;
    private final BasketService basketService = BasketService.getInstance();

    private final ObservableList<Costume> items = FXCollections.observableArrayList();
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<LocalDate> endDate =
            new SimpleObjectProperty<>(LocalDate.now().plusDays(1));
    private final StringProperty totalPrice = new SimpleStringProperty("0.00 грн");
    private final StringProperty depositTotal = new SimpleStringProperty("0.00 грн");
    private final StringProperty discountAmount = new SimpleStringProperty("0.00 грн");

    /**
     * Конструктор ViewModel кошика.
     *
     * @param rentalFacade фасад для оформлення оренди
     */
    public BasketViewModel(RentalFacade rentalFacade) {
        this.rentalFacade = rentalFacade;
        refresh();
    }

    /** Оновлює список елементів у кошику та перераховує вартість. */
    public void refresh() {
        if (javafx.application.Platform.isFxApplicationThread()) {
            items.setAll(basketService.getItems());
            recalculate();
        } else {
            javafx.application.Platform.runLater(() -> {
                items.setAll(basketService.getItems());
                recalculate();
            });
        }
    }

    /** Перераховує загальну вартість оренди, заставу та знижку на основі вибраних дат. */
    public void recalculate() {
        if (!javafx.application.Platform.isFxApplicationThread()) {
            javafx.application.Platform.runLater(this::recalculate);
            return;
        }
        
        if (startDate.get() != null && endDate.get() != null) {
            long days = ChronoUnit.DAYS.between(startDate.get(), endDate.get());
            if (days <= 0) days = 1;

            BigDecimal rentalTotal = basketService.calculateRentalTotal(days);
            BigDecimal deposit = basketService.calculateTotalDeposit();
            BigDecimal discount = basketService.calculateDiscount(days);

            totalPrice.set(String.format("%.2f грн", rentalTotal.add(deposit)));
            depositTotal.set(String.format("%.2f грн", deposit));
            discountAmount.set(String.format("-%.2f грн", discount));
        }
    }

    /**
     * Оформлює замовлення (checkout). Створює записи про оренду в базі даних та очищує кошик.
     *
     * @throws IllegalStateException якщо користувач не авторизований або кошик порожній
     */
    public void checkout() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("Будь ласка, авторизуйтесь.");
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Кошик порожній.");
        }

        rentalFacade.checkout(user, items, startDate.get(), endDate.get());
        basketService.clear();
        refresh();
    }

    /**
     * @return ObservableList елементів у кошику
     */
    public ObservableList<Costume> getItems() {
        return items;
    }

    /**
     * @return властивість дати початку оренди
     */
    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }

    /**
     * @return властивість дати завершення оренди
     */
    public ObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }

    /**
     * @return властивість загальної вартості
     */
    public StringProperty totalPriceProperty() {
        return totalPrice;
    }

    /**
     * @return властивість загальної суми застави
     */
    public StringProperty depositTotalProperty() {
        return depositTotal;
    }

    /**
     * @return властивість суми знижки
     */
    public StringProperty discountAmountProperty() {
        return discountAmount;
    }

    /**
     * @return копія списку елементів у кошику
     */
    public List<Costume> getItemsSnapshot() {
        return basketService.getItems();
    }

    /**
     * Розраховує вартість оренди для вказаної кількості днів.
     *
     * @param days кількість днів оренди
     * @return сума вартості оренди
     */
    public BigDecimal getRentalTotal(long days) {
        return basketService.calculateRentalTotal(days);
    }

    /**
     * @return сумарна сума застави за всі предмети
     */
    public BigDecimal getTotalDeposit() {
        return basketService.calculateTotalDeposit();
    }

    /**
     * Розраховує суму знижки.
     *
     * @param days кількість днів оренди
     * @return сума знижки
     */
    public BigDecimal getDiscount(long days) {
        return basketService.calculateDiscount(days);
    }
}
