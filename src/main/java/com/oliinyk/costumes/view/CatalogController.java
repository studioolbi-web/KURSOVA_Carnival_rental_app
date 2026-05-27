package com.oliinyk.costumes.view;

import com.oliinyk.costumes.model.Costume;
import com.oliinyk.costumes.repository.JdbcRentalRepository;
import com.oliinyk.costumes.service.SessionManager;
import com.oliinyk.costumes.viewmodel.CatalogViewModel;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

/**
 * Контролер каталогу костюмів. Відповідає за відображення списку доступних костюмів та їх
 * фільтрацію.
 */
public class CatalogController {

    @FXML private TilePane costumesGrid;
    @FXML private TextField searchField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button gridModeBtn;
    @FXML private Button listModeBtn;

    private CatalogViewModel viewModel;
    private final JdbcRentalRepository rentalRepo = new JdbcRentalRepository();

    /** Встановлює ViewModel та ініціалізує дані. */
    public void setViewModel(CatalogViewModel viewModel) {
        this.viewModel = viewModel;
        setupUI();
        setupSearch();
        setupViewModeListener();
        populateGrid(viewModel.getCostumes());
    }

    private void setupUI() {
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusDays(1));

        // Налаштування обмежень для дат (Вимога UI/UX)
        startDatePicker.setDayCellFactory(
                picker ->
                        new DateCell() {
                            @Override
                            public void updateItem(LocalDate date, boolean empty) {
                                super.updateItem(date, empty);
                                setDisable(empty || date.isBefore(LocalDate.now()));
                            }
                        });

        endDatePicker.setDayCellFactory(
                picker ->
                        new DateCell() {
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
                        (obs, old, newVal) -> {
                            if (newVal != null
                                    && (endDatePicker.getValue() == null
                                            || endDatePicker
                                                    .getValue()
                                                    .isBefore(newVal.plusDays(1)))) {
                                endDatePicker.setValue(newVal.plusDays(1));
                            }
                            refreshCatalog();
                        });
        endDatePicker.valueProperty().addListener((obs, old, newVal) -> refreshCatalog());

        gridModeBtn.setOnAction(e -> SessionManager.getInstance().viewModeProperty().set("GRID"));
        listModeBtn.setOnAction(e -> SessionManager.getInstance().viewModeProperty().set("LIST"));
    }

    private void refreshCatalog() {
        populateGrid(viewModel.getCostumes());
    }

    private void setupViewModeListener() {
        SessionManager.getInstance()
                .viewModeProperty()
                .addListener(
                        (obs, oldVal, newVal) -> {
                            populateGrid(viewModel.getCostumes());
                        });
    }

    private void setupSearch() {
        // Реалізація динамічного пошуку (Вимога 5.2)
        searchField
                .textProperty()
                .addListener(
                        (obs, oldText, newText) -> {
                            populateGrid(viewModel.getCostumes());
                        });
    }

    private void populateGrid(List<Costume> costumes) {
        costumesGrid.getChildren().clear();
        String viewMode = SessionManager.getInstance().viewModeProperty().get();

        if ("LIST".equals(viewMode)) {
            costumesGrid.setPrefColumns(1);
        } else {
            costumesGrid.setPrefColumns(3);
        }

        // Фільтрація за пошуком
        String filter = searchField.getText();
        List<Costume> filtered =
                costumes.stream()
                        .filter(
                                c ->
                                        filter == null
                                                || filter.isEmpty()
                                                || c.getName()
                                                        .toLowerCase()
                                                        .contains(filter.toLowerCase()))
                        .collect(Collectors.toList());

        for (Costume costume : filtered) {
            costumesGrid.getChildren().add(createCostumeCard(costume, viewMode));
        }
    }

    private VBox createCostumeCard(Costume costume, String viewMode) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("card");

        // Перевірка доступності
        boolean isAvailable = true;
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            isAvailable =
                    rentalRepo.isCostumeAvailable(
                            costume.getId(), startDatePicker.getValue(), endDatePicker.getValue());
        }

        if ("LIST".equals(viewMode)) {
            card.setStyle(
                    "-fx-background-color: -color-bg-subtle; -fx-background-radius: 12; -fx-border-color: -color-border-default; -fx-border-radius: 12; -fx-border-width: 1; -fx-pref-width: 800; -fx-pref-height: 120;");
            card.setAlignment(Pos.CENTER_LEFT);
        } else {
            card.setStyle(
                    "-fx-background-color: -color-bg-subtle; -fx-background-radius: 12; -fx-border-color: -color-border-default; -fx-border-radius: 12; -fx-border-width: 1; -fx-pref-width: 280; -fx-pref-height: 250;");
            card.setAlignment(Pos.CENTER);
        }

        if (!isAvailable) {
            card.setOpacity(0.6);
        }

        Label nameLabel = new Label(costume.getName());
        nameLabel.setStyle(
                "-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: -color-fg-default;");

        ImageView imageView = new ImageView();
        if (costume.getImagePath() != null && !costume.getImagePath().isEmpty()) {
            try {
                String path = costume.getImagePath();
                // 1. Спробувати завантажити як ресурс з classpath
                java.net.URL resourceUrl = getClass().getResource(path);
                if (resourceUrl != null) {
                    imageView.setImage(new Image(resourceUrl.toExternalForm(), true));
                } else {
                    // 2. Спробувати завантажити як файл із файлової системи
                    java.io.File file = new java.io.File(path);
                    if (file.exists()) {
                        imageView.setImage(new Image(file.toURI().toString(), true));
                    }
                }

                imageView.setFitWidth("LIST".equals(viewMode) ? 80 : 150);
                imageView.setFitHeight("LIST".equals(viewMode) ? 80 : 150);
                imageView.setPreserveRatio(true);
            } catch (Exception ex) {
                // Логування помилки завантаження зображення
            }
        }

        Label priceLabel = new Label(String.format("%.2f", costume.getPricePerDay()) + " грн/день");
        priceLabel.setStyle(
                "-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: -color-accent-emphasis;");

        Button addToCartBtn = new Button(isAvailable ? "Додати в кошик" : "Недоступно");
        addToCartBtn.getStyleClass().add(isAvailable ? "accent" : "danger");
        addToCartBtn.setDisable(!isAvailable);
        addToCartBtn.setMaxWidth(Double.MAX_VALUE);
        addToCartBtn.setOnAction(
                e -> {
                    viewModel.addToCart(costume);
                    addToCartBtn.setText("Додано!");
                    addToCartBtn.getStyleClass().remove("accent");
                    addToCartBtn.getStyleClass().add("success");
                    addToCartBtn.setDisable(true);
                });

        if ("LIST".equals(viewMode)) {
            javafx.scene.layout.HBox hBox =
                    new javafx.scene.layout.HBox(
                            20, imageView, nameLabel, priceLabel, addToCartBtn);
            hBox.setAlignment(Pos.CENTER_LEFT);
            javafx.scene.layout.HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);
            nameLabel.setMaxWidth(Double.MAX_VALUE);
            card.getChildren().add(hBox);
        } else {
            card.getChildren().addAll(imageView, nameLabel, priceLabel, addToCartBtn);
        }

        return card;
    }
}
