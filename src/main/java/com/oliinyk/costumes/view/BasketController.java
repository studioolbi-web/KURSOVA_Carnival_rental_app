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

    @FXML private ListView<Costume> basketItems;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label totalPriceLabel;
    @FXML private Label depositLabel;
    @FXML private Label discountLabel;

    private BasketViewModel viewModel;

    public void setViewModel(BasketViewModel viewModel) {
        this.viewModel = viewModel;
        
        basketItems.setCellFactory(param -> new javafx.scene.control.ListCell<Costume>() {
            private final javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
            @Override
            protected void updateItem(Costume item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
                        try {
                            String path = item.getImagePath();
                            java.net.URL resourceUrl = getClass().getResource(path);
                            if (resourceUrl != null) {
                                imageView.setImage(new javafx.scene.image.Image(resourceUrl.toExternalForm(), true));
                            } else {
                                java.io.File file = new java.io.File(path);
                                if (file.exists()) {
                                    imageView.setImage(new javafx.scene.image.Image(file.toURI().toString(), true));
                                }
                            }
                            imageView.setFitWidth(50);
                            imageView.setFitHeight(50);
                            imageView.setPreserveRatio(true);
                        } catch (Exception ex) {}
                    } else {
                        imageView.setImage(null);
                    }
                    
                    VBox vBox = new VBox(5);
                    Label nameLabel = new Label(item.getName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: -color-fg-default;");
                    Label priceLabel = new Label(String.format("%.2f грн/день", item.getPricePerDay()));
                    priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-accent-emphasis;");
                    
                    vBox.getChildren().addAll(nameLabel, priceLabel);
                    
                    HBox hBox = new HBox(15);
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    hBox.getChildren().addAll(imageView, vBox);
                    
                    setText(null);
                    setGraphic(hBox);
                }
            }
        });

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
        if (javafx.application.Platform.isFxApplicationThread()) {
            basketItems.getItems().setAll(viewModel.getItems());
        } else {
            javafx.application.Platform.runLater(() -> basketItems.getItems().setAll(viewModel.getItems()));
        }
    }

    @FXML
    private void onRentClicked() {
        // Знімок даних кошика до очищення (Блок 1: Електронний чек)
        List<Costume> snapshot = viewModel.getItemsSnapshot();
        long days = ChronoUnit.DAYS.between(startDatePicker.getValue(), endDatePicker.getValue());
        if (days <= 0) days = 1;
        BigDecimal rentalTotal = viewModel.getRentalTotal(days);
        BigDecimal deposit = viewModel.getTotalDeposit();
        BigDecimal discount = viewModel.getDiscount(days);
        BigDecimal grandTotal = rentalTotal.add(deposit).subtract(discount);
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

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
                    refreshList();

                    // Показати електронний чек
                    showReceiptDialog(
                            snapshot,
                            start,
                            end,
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
        long calculatedDays = ChronoUnit.DAYS.between(start, end);
        final long days = calculatedDays <= 0 ? 1 : calculatedDays;

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
        closeBtn.getStyleClass().addAll("button");
        closeBtn.setPrefWidth(160);

        // ── Кнопка Зберегти PDF ──
        Button savePdfBtn = new Button("Зберегти PDF");
        savePdfBtn.getStyleClass().addAll("button", "accent");
        savePdfBtn.setPrefWidth(160);

        savePdfBtn.setOnAction(ev -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Зберегти чек у PDF");
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Document", "*.pdf"));
            fileChooser.setInitialFileName("receipt_" + System.currentTimeMillis() + ".pdf");
            java.io.File file = fileChooser.showSaveDialog(basketItems.getScene().getWindow());

            if (file != null) {
                com.oliinyk.costumes.service.export.ReceiptData data = new com.oliinyk.costumes.service.export.ReceiptData(
                        items, start, end, days, rentalTotal, deposit, discount, grandTotal
                );
                com.oliinyk.costumes.service.export.ExportStrategy<com.oliinyk.costumes.service.export.ReceiptData> strategy = new com.oliinyk.costumes.service.export.ReceiptPdfExportStrategy();
                try {
                    strategy.export(data, file);
                    showAlert("Успіх", "Чек успішно збережено в PDF!", Alert.AlertType.INFORMATION);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("Помилка", "Не вдалося зберегти PDF: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });

        HBox buttonsBox = new HBox(10, closeBtn, savePdfBtn);
        buttonsBox.setAlignment(Pos.CENTER);

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
                        buttonsBox);
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
