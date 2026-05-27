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
    @FXML private ComboBox<com.oliinyk.costumes.model.Category> categoryFilter;
    
    @FXML private javafx.scene.control.Label titleLabel;
    @FXML private javafx.scene.control.Label periodLabel;
    @FXML private javafx.scene.control.Tooltip gridTooltip;
    @FXML private javafx.scene.control.Tooltip listTooltip;

    private CatalogViewModel viewModel;
    private final JdbcRentalRepository rentalRepo = new JdbcRentalRepository();
    private final com.oliinyk.costumes.repository.JdbcCategoryRepository categoryRepo = new com.oliinyk.costumes.repository.JdbcCategoryRepository();

    /** Встановлює ViewModel та ініціалізує дані. */
    public void setViewModel(CatalogViewModel viewModel) {
        this.viewModel = viewModel;
        setupUI();
        setupSearch();
        setupViewModeListener();
        populateGrid(viewModel.getCostumes());
    }

    private void setupUI() {
        titleLabel.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("catalog.title"));
        periodLabel.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("catalog.period"));
        startDatePicker.promptTextProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("catalog.from"));
        endDatePicker.promptTextProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("catalog.to"));
        gridTooltip.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("catalog.grid"));
        listTooltip.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("catalog.list"));
        categoryFilter.promptTextProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("catalog.all_categories"));
        searchField.promptTextProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("catalog.search"));

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
        // Завантаження категорій
        categoryFilter.getItems().add(null); // Опція "Всі категорії"
        categoryFilter.getItems().addAll(categoryRepo.findAll());
        categoryFilter.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(com.oliinyk.costumes.model.Category category) {
                return category == null ? com.oliinyk.costumes.util.I18nManager.get("catalog.all_categories") : category.getName();
            }
            @Override
            public com.oliinyk.costumes.model.Category fromString(String string) {
                return null;
            }
        });
        
        categoryFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> populateGrid(viewModel.getCostumes()));

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
        com.oliinyk.costumes.model.Category selectedCat = categoryFilter.getSelectionModel().getSelectedItem();

        List<Costume> filtered =
                costumes.stream()
                        .filter(c -> {
                            boolean matchesName = filter == null || filter.isEmpty() || c.getName().toLowerCase().contains(filter.toLowerCase());
                            boolean matchesCat = selectedCat == null || c.getCategoryId().equals(selectedCat.getId());
                            return matchesName && matchesCat;
                        })
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

        Button addToCartBtn = new Button();
        final boolean available = isAvailable;
        addToCartBtn.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!available) return com.oliinyk.costumes.util.I18nManager.get("catalog.unavailable");
            if (addToCartBtn.isDisabled()) return com.oliinyk.costumes.util.I18nManager.get("catalog.added");
            return com.oliinyk.costumes.util.I18nManager.get("catalog.add_to_cart");
        }, com.oliinyk.costumes.util.I18nManager.localeProperty(), addToCartBtn.disabledProperty()));
        
        addToCartBtn.getStyleClass().add(isAvailable ? "accent" : "danger");
        addToCartBtn.setDisable(!isAvailable);
        addToCartBtn.setMaxWidth(Double.MAX_VALUE);
        addToCartBtn.setOnAction(
                e -> {
                    viewModel.addToCart(costume);
                    addToCartBtn.getStyleClass().remove("accent");
                    addToCartBtn.getStyleClass().add("success");
                    addToCartBtn.setDisable(true);
                    
                    showToast(com.oliinyk.costumes.util.I18nManager.get("catalog.toast.added") + " «" + costume.getName() + "»");
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

        // Клік для перегляду деталей
        card.setOnMouseClicked(event -> {
            showCostumeDetails(costume);
        });

        return card;
    }

    private void showToast(String message) {
        javafx.scene.control.Label toast = new javafx.scene.control.Label(message);
        toast.setStyle("-fx-background-color: -color-success-emphasis; -fx-text-fill: -color-fg-on-emphasis; -fx-padding: 10 20; -fx-background-radius: 20; -fx-font-size: 14;");
        
        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.getContent().add(toast);
        popup.setAutoHide(true);
        
        javafx.stage.Window window = costumesGrid.getScene().getWindow();
        toast.applyCss();
        toast.layout();
        
        // Show initially to calculate size, then adjust position
        popup.show(window);
        double x = window.getX() + window.getWidth() / 2 - toast.prefWidth(-1) / 2;
        double y = window.getY() + window.getHeight() - 100;
        popup.setX(x);
        popup.setY(y);

        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
        delay.setOnFinished(e -> popup.hide());
        delay.play();
    }
    private void showCostumeDetails(Costume costume) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/CostumeDetailView.fxml"));
            javafx.scene.layout.VBox page = loader.load();

            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.setTitle(costume.getName());
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(costumesGrid.getScene().getWindow());
            javafx.scene.Scene scene = new javafx.scene.Scene(page, 500, 650);
            dialogStage.setScene(scene);

            CostumeDetailController controller = loader.getController();
            controller.setCostume(costume);

            dialogStage.showAndWait();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
