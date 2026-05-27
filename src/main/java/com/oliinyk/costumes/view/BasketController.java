package com.oliinyk.costumes.view;

import com.oliinyk.costumes.model.Costume;
import com.oliinyk.costumes.viewmodel.BasketViewModel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/** Контролер кошика. Реалізує асинхронну логіку оформлення замовлення (Вимога розділу 4.4.5). */
public class BasketController {

    @FXML private ListView<String> basketItems;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label totalPriceLabel;
    @FXML private Label depositLabel;
    @FXML private Label discountLabel;

    private BasketViewModel viewModel;

    public void setViewModel(BasketViewModel viewModel) {
        this.viewModel = viewModel;
        setupBindings();
        refreshList();
    }

    private void setupBindings() {
        startDatePicker.valueProperty().bindBidirectional(viewModel.startDateProperty());
        endDatePicker.valueProperty().bindBidirectional(viewModel.endDateProperty());
        totalPriceLabel.textProperty().bind(viewModel.totalPriceProperty());
        depositLabel.textProperty().bind(viewModel.depositTotalProperty());
        discountLabel.textProperty().bind(viewModel.discountAmountProperty());

        // Налаштування обмежень для дат (Вимога UI/UX)
        startDatePicker.setDayCellFactory(
                picker ->
                        new javafx.scene.control.DateCell() {
                            @Override
                            public void updateItem(LocalDate date, boolean empty) {
                                super.updateItem(date, empty);
                                setDisable(empty || date.isBefore(LocalDate.now()));
                            }
                        });

        endDatePicker.setDayCellFactory(
                picker ->
                        new javafx.scene.control.DateCell() {
                            @Override
                            public void updateItem(LocalDate date, boolean empty) {
                                super.updateItem(date, empty);
                                setDisable(
                                        empty
                                                || date.isBefore(
                                                        startDatePicker.getValue().plusDays(1)));
                            }
                        });

        startDatePicker
                .valueProperty()
                .addListener(
                        (obs, oldVal, newVal) -> {
                            if (newVal != null
                                    && (endDatePicker.getValue() == null
                                            || endDatePicker
                                                    .getValue()
                                                    .isBefore(newVal.plusDays(1)))) {
                                endDatePicker.setValue(newVal.plusDays(1));
                            }
                            viewModel.recalculate();
                        });
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> viewModel.recalculate());
    }

    private void refreshList() {
        basketItems.getItems().clear();
        for (Costume c : viewModel.getItems()) {
            basketItems
                    .getItems()
                    .add(String.format("%s - %.2f грн/день", c.getName(), c.getPricePerDay()));
        }
    }

    @FXML
    private void onRentClicked() {
        // Асинхронне оформлення замовлення (Вимога розділу 4.4.5)
        Task<Void> checkoutTask =
                new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        viewModel.checkout();
                        return null;
                    }
                };
        checkoutTask.setOnSucceeded(
                e -> {
                    // Знімок даних кошика до очищення (Блок 1: Електронний чек)
                    List<Costume> snapshot = viewModel.getItemsSnapshot();
                    long days =
                            ChronoUnit.DAYS.between(
                                    startDatePicker.getValue(), endDatePicker.getValue());
                    if (days <= 0) days = 1;
                    BigDecimal rentalTotal = viewModel.getRentalTotal(days);
                    BigDecimal deposit = viewModel.getTotalDeposit();
                    BigDecimal discount = viewModel.getDiscount(days);
                    BigDecimal grandTotal = rentalTotal.add(deposit);

                    refreshList();

                    // Показати електронний чек
                    showReceiptDialog(
                            snapshot,
                            startDatePicker.getValue(),
                            endDatePicker.getValue(),
                            rentalTotal,
                            deposit,
                            discount,
                            grandTotal);

                    // Оновлення стану через SessionManager
                    com.oliinyk.costumes.service.SessionManager.getInstance()
                            .viewModeProperty()
                            .set(
                                    com.oliinyk.costumes.service.SessionManager.getInstance()
                                            .viewModeProperty()
                                            .get());
                });

        checkoutTask.setOnFailed(
                e -> {
                    Throwable ex = checkoutTask.getException();
                    showAlert("Помилка", ex.getMessage(), Alert.AlertType.ERROR);
                });

        // Запуск таска у фоновому потоці
        new Thread(checkoutTask).start();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Показує модальне вікно електронного чеку після успішного оформлення оренди. Вікно генерується
     * програмно через компоненти JavaFX (без FXML).
     */
    private void showReceiptDialog(
            List<Costume> items,
            LocalDate start,
            LocalDate end,
            BigDecimal rentalTotal,
            BigDecimal deposit,
            BigDecimal discount,
            BigDecimal grandTotal) {

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        long days = ChronoUnit.DAYS.between(start, end);
        if (days <= 0) days = 1;

        // ── Заголовок чеку ──
        Label titleIcon = new Label("\uD83E\uDDFE");
        titleIcon.setStyle("-fx-font-size: 28px;");
        Label titleLabel = new Label("Електронний чек");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        HBox titleBox = new HBox(10, titleIcon, titleLabel);
        titleBox.setAlignment(Pos.CENTER);

        Label subLabel = new Label("Карнавальна крамниця — оренда костюмів");
        subLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");
        subLabel.setAlignment(Pos.CENTER);
        subLabel.setMaxWidth(Double.MAX_VALUE);

        // ── Дати ──
        Label datesLabel =
                new Label(
                        "\uD83D\uDCC5  "
                                + start.format(fmt)
                                + "  →  "
                                + end.format(fmt)
                                + "   ("
                                + days
                                + " дн.)");
        datesLabel.setStyle("-fx-font-size: 13px;");

        // ── Список костюмів ──
        VBox itemsBox = new VBox(6);
        for (Costume c : items) {
            BigDecimal lineTotal = c.getPricePerDay().multiply(BigDecimal.valueOf(days));
            Label row =
                    new Label(
                            String.format(
                                    "• %s  —  %.2f грн/день  ×%d  =  %.2f грн",
                                    c.getName(), c.getPricePerDay(), days, lineTotal));
            row.setStyle("-fx-font-size: 13px;");
            itemsBox.getChildren().add(row);
        }

        // ── Фінансова зведення ──
        VBox summaryBox = new VBox(8);
        summaryBox.setStyle(
                "-fx-background-color: -color-bg-subtle;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 12;");

        summaryBox
                .getChildren()
                .addAll(
                        buildSummaryRow(
                                "Вартість оренди:", String.format("%.2f грн", rentalTotal), false),
                        buildSummaryRow("Застава:", String.format("%.2f грн", deposit), false),
                        buildSummaryRow(
                                "Знижка (10% від 3+ дн.):",
                                String.format("-%.2f грн", discount),
                                discount.compareTo(BigDecimal.ZERO) > 0),
                        new Separator(),
                        buildSummaryRow(
                                "До сплати:", String.format("%.2f грн", grandTotal), false));

        // Виділити підсумок жирним
        HBox totalRow = (HBox) summaryBox.getChildren().get(summaryBox.getChildren().size() - 1);
        totalRow.getChildren()
                .forEach(
                        node -> {
                            if (node instanceof Label lbl) {
                                lbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
                            }
                        });

        // ── Кнопка закриття ──
        Button closeBtn = new Button("Закрити");
        closeBtn.getStyleClass().addAll("button", "accent");
        closeBtn.setPrefWidth(160);

        // ── Складання макету ──
        VBox root =
                new VBox(
                        14,
                        titleBox,
                        subLabel,
                        new Separator(),
                        datesLabel,
                        new Separator(),
                        itemsBox,
                        new Separator(),
                        summaryBox,
                        closeBtn);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: -color-bg-default;");
        root.setPrefWidth(460);

        Stage dialog = new Stage();
        dialog.setTitle("Електронний чек");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(basketItems.getScene().getWindow());
        dialog.setResizable(false);

        closeBtn.setOnAction(ev -> dialog.close());

        Scene scene = new Scene(root);
        // Успадкувати стилі з головного вікна
        scene.getStylesheets().addAll(basketItems.getScene().getStylesheets());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /** Будує рядок зведення (label + значення) для чеку. */
    private HBox buildSummaryRow(String label, String value, boolean isDiscount) {
        Label left = new Label(label);
        left.setStyle("-fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label right = new Label(value);
        right.setStyle(
                "-fx-font-size: 13px;" + (isDiscount ? " -fx-text-fill: -color-success-fg;" : ""));

        HBox row = new HBox(spacer);
        row.getChildren().add(0, left);
        row.getChildren().add(right);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
