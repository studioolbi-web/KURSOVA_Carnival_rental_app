package com.oliinyk.costumes.view;

import com.oliinyk.costumes.dto.RentalDTO;
import com.oliinyk.costumes.model.Costume;
import com.oliinyk.costumes.model.User;
import com.oliinyk.costumes.repository.JdbcCostumeRepository;
import com.oliinyk.costumes.repository.JdbcRentalItemRepository;
import com.oliinyk.costumes.repository.JdbcRentalRepository;
import com.oliinyk.costumes.repository.JdbcUserRepository;
import com.oliinyk.costumes.service.RentalFacade;
import com.oliinyk.costumes.service.RentalService;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Контролер панелі адміністратора. Реалізує CRUD операції, асинхронне завантаження та фільтрацію
 * (Вимоги 5.1, 5.2, 4.4.5).
 */
public class AdminPanelController {

    @FXML private TableView<Costume> costumesTable;
    @FXML private TableView<User> usersTable;
    @FXML private TableView<RentalDTO> rentalsTable;
    @FXML private TextField costumeSearchField;
    @FXML private javafx.scene.chart.PieChart statusChart;
    @FXML private javafx.scene.chart.BarChart<String, Number> popularityChart;

    private final JdbcCostumeRepository costumeRepo = new JdbcCostumeRepository();
    private final JdbcUserRepository userRepo = new JdbcUserRepository();
    private final com.oliinyk.costumes.service.ReportService reportService =
            new com.oliinyk.costumes.service.ReportService();
    private RentalFacade rentalFacade;

    private ObservableList<Costume> costumesData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Ініціалізація фасаду
        rentalFacade =
                new RentalFacade(
                        new RentalService(
                                new JdbcRentalRepository(), new JdbcRentalItemRepository()),
                        new JdbcRentalRepository(),
                        new JdbcCostumeRepository(),
                        new JdbcUserRepository());

        setupTables();
        loadDataAsync();
        setupSearch();

        // Візуальний баг 1: Поворот тексту на осі X (Вимога UX)
        javafx.scene.chart.CategoryAxis xAxis =
                (javafx.scene.chart.CategoryAxis) popularityChart.getXAxis();
        xAxis.setTickLabelRotation(-45);
    }

    private void setupTables() {
        setupCostumesTable();
        setupUsersTable();
        setupRentalsTable();
    }

    private void setupCostumesTable() {
        TableColumn<Costume, String> nameCol =
                (TableColumn<Costume, String>) costumesTable.getColumns().get(0);
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn<Costume, String> descCol =
                (TableColumn<Costume, String>) costumesTable.getColumns().get(1);
        descCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getDescription()));

        TableColumn<Costume, String> priceCol =
                (TableColumn<Costume, String>) costumesTable.getColumns().get(2);
        priceCol.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                String.format("%.2f грн", data.getValue().getPricePerDay())));

        // Додавання кнопок дій (Вимога 5.1 CRUD)
        TableColumn<Costume, Void> actionCol =
                (TableColumn<Costume, Void>) costumesTable.getColumns().get(3);
        actionCol.setCellFactory(
                param ->
                        new javafx.scene.control.TableCell<>() {
                            private final javafx.scene.control.Button editBtn =
                                    new javafx.scene.control.Button();
                            private final javafx.scene.control.Button deleteBtn =
                                    new javafx.scene.control.Button();
                            private final javafx.scene.layout.HBox pane =
                                    new javafx.scene.layout.HBox(10, editBtn, deleteBtn);

                            {
                                editBtn.getStyleClass().addAll("button-icon", "flat", "accent");
                                editBtn.setGraphic(
                                        new org.kordamp.ikonli.javafx.FontIcon("fas-edit"));
                                editBtn.setTooltip(new javafx.scene.control.Tooltip("Редагувати"));
                                editBtn.setOnAction(
                                        event -> {
                                            Costume costume =
                                                    getTableView().getItems().get(getIndex());
                                            handleEditCostume(costume);
                                        });

                                deleteBtn.getStyleClass().addAll("button-icon", "flat", "danger");
                                deleteBtn.setGraphic(
                                        new org.kordamp.ikonli.javafx.FontIcon("fas-trash"));
                                deleteBtn.setTooltip(new javafx.scene.control.Tooltip("Видалити"));
                                deleteBtn.setOnAction(
                                        event -> {
                                            Costume costume =
                                                    getTableView().getItems().get(getIndex());
                                            handleDeleteCostume(costume);
                                        });
                                pane.setAlignment(javafx.geometry.Pos.CENTER);
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) setGraphic(null);
                                else setGraphic(pane);
                            }
                        });
    }

    @FXML
    private void onAddCostumeClicked() {
        showCostumeDialog(null);
    }

    private void handleEditCostume(Costume costume) {
        showCostumeDialog(costume);
    }

    private void showCostumeDialog(Costume costume) {
        try {
            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(getClass().getResource("/views/CostumeDialog.fxml"));
            javafx.scene.layout.VBox page = loader.load();

            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.setTitle(costume == null ? "Додати костюм" : "Редагувати костюм");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(costumesTable.getScene().getWindow());
            javafx.scene.Scene scene = new javafx.scene.Scene(page);
            dialogStage.setScene(scene);

            CostumeDialogController controller = loader.getController();
            controller.setCostume(costume);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                Costume result = controller.getCostume();
                if (costume == null) {
                    costumeRepo.save(result);
                } else {
                    costumeRepo.update(result);
                }
                loadDataAsync();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteCostume(Costume costume) {
        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження видалення");
        alert.setHeaderText("Видалити костюм: " + costume.getName() + "?");
        alert.setContentText("Цю дію неможливо скасувати.");

        alert.showAndWait()
                .ifPresent(
                        response -> {
                            if (response == javafx.scene.control.ButtonType.OK) {
                                try {
                                    costumeRepo.delete(costume.getId());
                                    loadDataAsync(); // Перезавантаження даних
                                } catch (Exception e) {
                                    // Критичний баг 2: Обробка
                                    // SQLIntegrityConstraintViolationException (Вимога
                                    // стабільності)
                                    javafx.scene.control.Alert errorAlert =
                                            new javafx.scene.control.Alert(
                                                    javafx.scene.control.Alert.AlertType.ERROR);
                                    errorAlert.setTitle("Помилка видалення");
                                    errorAlert.setHeaderText(null);
                                    errorAlert.setContentText(
                                            "Неможливо видалити костюм, оскільки він присутній в історії оренд користувачів");
                                    errorAlert.showAndWait();
                                }
                            }
                        });
    }

    private void setupUsersTable() {
        TableColumn<User, String> emailCol =
                (TableColumn<User, String>) usersTable.getColumns().get(0);
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));

        TableColumn<User, String> roleCol =
                (TableColumn<User, String>) usersTable.getColumns().get(1);
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));

        TableColumn<User, String> verifiedCol =
                (TableColumn<User, String>) usersTable.getColumns().get(2);
        verifiedCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isVerified() ? "Так" : "Ні"));
        verifiedCol.setCellFactory(
                column ->
                        new javafx.scene.control.TableCell<>() {
                            @Override
                            protected void updateItem(String val, boolean empty) {
                                super.updateItem(val, empty);
                                if (empty || val == null) {
                                    setGraphic(null);
                                } else {
                                    javafx.scene.control.Label badge =
                                            new javafx.scene.control.Label(val);
                                    badge.getStyleClass()
                                            .addAll(
                                                    "badge",
                                                    "Так".equals(val) ? "success" : "subtle");
                                    setGraphic(badge);
                                }
                            }
                        });

        TableColumn<User, String> blockedCol = new TableColumn<>("Заблоковано");
        blockedCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isBlocked() ? "Так" : "Ні"));
        blockedCol.setCellFactory(
                column ->
                        new javafx.scene.control.TableCell<>() {
                            @Override
                            protected void updateItem(String val, boolean empty) {
                                super.updateItem(val, empty);
                                if (empty || val == null) {
                                    setGraphic(null);
                                } else {
                                    javafx.scene.control.Label badge =
                                            new javafx.scene.control.Label(val);
                                    badge.getStyleClass()
                                            .addAll(
                                                    "badge",
                                                    "Так".equals(val) ? "danger" : "subtle");
                                    setGraphic(badge);
                                }
                            }
                        });

        TableColumn<User, Void> actionCol = new TableColumn<>("Дії");
        actionCol.setCellFactory(
                param ->
                        new javafx.scene.control.TableCell<>() {
                            private final javafx.scene.control.Button blockBtn =
                                    new javafx.scene.control.Button();

                            {
                                blockBtn.getStyleClass().add("button-icon");
                                blockBtn.setOnAction(
                                        event -> {
                                            User user = getTableView().getItems().get(getIndex());
                                            handleToggleBlock(user);
                                        });
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    User user = getTableView().getItems().get(getIndex());
                                    if (user.isBlocked()) {
                                        // Заблокований -> дія "Розблокувати" (Відкритий замок,
                                        // зелений)
                                        blockBtn.setGraphic(
                                                new org.kordamp.ikonli.javafx.FontIcon(
                                                        "fas-lock-open"));
                                        blockBtn.getStyleClass()
                                                .setAll("button-icon", "flat", "success");
                                        blockBtn.setTooltip(
                                                new javafx.scene.control.Tooltip("Розблокувати"));
                                    } else {
                                        // Активний -> дія "Заблокувати" (Закритий замок, червоний)
                                        blockBtn.setGraphic(
                                                new org.kordamp.ikonli.javafx.FontIcon("fas-lock"));
                                        blockBtn.getStyleClass()
                                                .setAll("button-icon", "flat", "danger");
                                        blockBtn.setTooltip(
                                                new javafx.scene.control.Tooltip("Заблокувати"));
                                    }
                                    setGraphic(blockBtn);
                                }
                            }
                        });

        usersTable.getColumns().addAll(blockedCol, actionCol);
    }

    private void handleToggleBlock(User user) {
        user.setBlocked(!user.isBlocked());
        userRepo.update(user);
        usersTable.refresh(); // Миттєве оновлення UI (Вимога розділу 6)
    }

    private void setupRentalsTable() {
        TableColumn<RentalDTO, String> userCol =
                (TableColumn<RentalDTO, String>) rentalsTable.getColumns().get(0);
        userCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getUserEmail()));

        TableColumn<RentalDTO, String> startCol =
                (TableColumn<RentalDTO, String>) rentalsTable.getColumns().get(1);
        startCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getStartDate().toString()));

        TableColumn<RentalDTO, String> endCol =
                (TableColumn<RentalDTO, String>) rentalsTable.getColumns().get(2);
        endCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getEndDate().toString()));

        TableColumn<RentalDTO, String> statusCol =
                (TableColumn<RentalDTO, String>) rentalsTable.getColumns().get(3);
        statusCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setCellFactory(
                column ->
                        new javafx.scene.control.TableCell<>() {
                            @Override
                            protected void updateItem(String status, boolean empty) {
                                super.updateItem(status, empty);
                                if (empty || status == null) {
                                    setText(null);
                                    setGraphic(null);
                                } else {
                                    javafx.scene.control.Label badge =
                                            new javafx.scene.control.Label();
                                    switch (status) {
                                        case "RESERVED" -> {
                                            badge.setText("Заброньовано");
                                            badge.getStyleClass().addAll("badge", "accent");
                                        }
                                        case "ISSUED", "ACTIVE" -> {
                                            badge.setText("Видано / Активна");
                                            badge.getStyleClass().addAll("badge", "warning");
                                        }
                                        case "OVERDUE" -> {
                                            badge.setText("Прострочено");
                                            badge.getStyleClass().addAll("badge", "danger");
                                        }
                                        case "RETURNED", "COMPLETED" -> {
                                            badge.setText("Повернуто / Завершена");
                                            badge.getStyleClass().addAll("badge", "success");
                                        }
                                        default -> {
                                            badge.setText(status);
                                            badge.getStyleClass().add("badge");
                                        }
                                    }
                                    setGraphic(badge);
                                }
                            }
                        });

        TableColumn<RentalDTO, String> totalCol =
                (TableColumn<RentalDTO, String>) rentalsTable.getColumns().get(4);
        totalCol.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                String.format("%.2f грн", data.getValue().getTotalPrice())));

        TableColumn<RentalDTO, String> penaltyCol = new TableColumn<>("Штраф");
        penaltyCol.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                String.format("%.2f грн", data.getValue().getPenaltyAmount())));
        penaltyCol.setPrefWidth(100);

        // Додавання ComboBox для зміни статусу (Блок 2: Машина станів)
        TableColumn<RentalDTO, Void> actionCol = new TableColumn<>("Дії");
        actionCol.setPrefWidth(220);
        actionCol.setCellFactory(
                param ->
                        new javafx.scene.control.TableCell<>() {
                            private final ComboBox<String> statusCombo = new ComboBox<>();

                            {
                                statusCombo.getItems().addAll("RESERVED", "ISSUED", "RETURNED");
                                statusCombo.getStyleClass().add("small");

                                // UX баг 3: StringConverter для локалізації статусів (Вимога UX)
                                statusCombo.setConverter(
                                        new javafx.util.StringConverter<>() {
                                            @Override
                                            public String toString(String status) {
                                                if (status == null) return "";
                                                return switch (status) {
                                                    case "RESERVED" -> "Заброньовано";
                                                    case "ISSUED" -> "Видано";
                                                    case "RETURNED" -> "Повернуто";
                                                    case "ACTIVE" -> "Активна";
                                                    case "COMPLETED" -> "Завершена";
                                                    case "OVERDUE" -> "Прострочено";
                                                    default -> status;
                                                };
                                            }

                                            @Override
                                            public String fromString(String string) {
                                                if (string == null) return null;
                                                return switch (string) {
                                                    case "Заброньовано" -> "RESERVED";
                                                    case "Видано" -> "ISSUED";
                                                    case "Повернуто" -> "RETURNED";
                                                    case "Активна" -> "ACTIVE";
                                                    case "Завершена" -> "COMPLETED";
                                                    case "Прострочено" -> "OVERDUE";
                                                    default -> string;
                                                };
                                            }
                                        });

                                statusCombo.setOnAction(
                                        event -> {
                                            RentalDTO rental =
                                                    getTableView().getItems().get(getIndex());
                                            String newValue = statusCombo.getValue();
                                            if (newValue != null
                                                    && !newValue.equals(rental.getStatus())) {
                                                handleStatusChange(rental, newValue);
                                            }
                                        });
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    RentalDTO rental = getTableView().getItems().get(getIndex());
                                    statusCombo.setValue(rental.getStatus());
                                    setGraphic(statusCombo);
                                }
                            }
                        });
        rentalsTable.getColumns().addAll(penaltyCol, actionCol);
    }

    @FXML
    private void onExportRentalsClicked() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Зберегти звіт");
        fileChooser
                .getExtensionFilters()
                .add(new javafx.stage.FileChooser.ExtensionFilter("CSV файли", "*.csv"));
        fileChooser.setInitialFileName("rentals_report_" + java.time.LocalDate.now() + ".csv");

        java.io.File file = fileChooser.showSaveDialog(rentalsTable.getScene().getWindow());
        if (file != null) {
            try {
                reportService.exportRentalsToCsv(rentalsTable.getItems(), file);
                javafx.scene.control.Alert alert =
                        new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Успіх");
                alert.setHeaderText(null);
                alert.setContentText("Звіт успішно експортовано у " + file.getName());
                alert.showAndWait();
            } catch (java.io.IOException e) {
                e.printStackTrace();
                javafx.scene.control.Alert alert =
                        new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Помилка");
                alert.setHeaderText("Не вдалося зберегти файл");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void handleStatusChange(RentalDTO rentalDto, String newStatus) {
        rentalFacade.updateStatus(rentalDto.getId(), newStatus);
        loadDataAsync();
    }

    private void updateCharts(List<RentalDTO> rentals) {
        // Статистика за статусами (Блок 3: Дашборд)
        java.util.Map<String, Long> statusCounts =
                rentals.stream()
                        .collect(
                                java.util.stream.Collectors.groupingBy(
                                        RentalDTO::getStatus,
                                        java.util.stream.Collectors.counting()));

        statusChart.getData().clear();
        // Візуальний баг 3: Стабільний розмір (Вимога UX)
        statusChart.setLabelsVisible(false);
        statusChart.setLegendVisible(true);

        statusCounts.forEach(
                (status, count) -> {
                    // UX баг 2: Локалізація в графіках (Вимога UX)
                    String localizedStatus =
                            switch (status) {
                                case "RESERVED" -> "Заброньовано";
                                case "ISSUED" -> "Видано";
                                case "RETURNED" -> "Повернуто";
                                case "COMPLETED" -> "Завершена";
                                case "ACTIVE" -> "Активна";
                                case "OVERDUE" -> "Прострочено";
                                default -> status;
                            };
                    statusChart
                            .getData()
                            .add(new javafx.scene.chart.PieChart.Data(localizedStatus, count));
                });

        // Популярність костюмів (Топ-5)
        java.util.Map<String, Long> costumePopularity =
                rentals.stream()
                        .flatMap(r -> r.getCostumeNames().stream())
                        .collect(
                                java.util.stream.Collectors.groupingBy(
                                        s -> s, java.util.stream.Collectors.counting()));

        popularityChart.getData().clear();
        // Візуальний баг 1: Налізання тексту та баг кешування (Вимога UX)
        javafx.scene.chart.CategoryAxis xAxis =
                (javafx.scene.chart.CategoryAxis) popularityChart.getXAxis();
        xAxis.getCategories().clear(); // Очищення кешу
        xAxis.setAnimated(false); // Вимикаємо анімацію лише для осі X
        xAxis.setTickLabelRotation(-45);
        popularityChart.setAnimated(true); // Залишаємо загальну анімацію для стовпців

        javafx.scene.chart.XYChart.Series<String, Number> series =
                new javafx.scene.chart.XYChart.Series<>();
        series.setName("Оренди");
        costumePopularity.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(
                        entry -> {
                            // Візуальний баг 1: Обрізання довгих назв (Вимога UX)
                            String name = entry.getKey();
                            String displayName =
                                    name.length() > 12 ? name.substring(0, 12) + ".." : name;
                            series.getData()
                                    .add(
                                            new javafx.scene.chart.XYChart.Data<>(
                                                    displayName, entry.getValue()));
                        });

        popularityChart.getData().add(series);
    }

    private void loadDataAsync() {
        // Асинхронне завантаження (Вимога розділу 4.4.5)
        Task<Void> loadTask =
                new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        List<Costume> costumes = costumeRepo.findAll();
                        List<User> users = userRepo.findAll();
                        List<RentalDTO> rentals = rentalFacade.getAllRentals();

                        // Оновлення UI в основному потоці
                        javafx.application.Platform.runLater(
                                () -> {
                                    costumesData.setAll(costumes);
                                    usersTable.setItems(FXCollections.observableArrayList(users));
                                    rentalsTable.setItems(
                                            FXCollections.observableArrayList(rentals));
                                    // Наповнення графіків дашборду (Блок 3: Дашборд)
                                    updateCharts(rentals);
                                });
                        return null;
                    }
                };
        new Thread(loadTask).start();
    }

    private void setupSearch() {
        // Реалізація пошуку (Вимога розділу 5.2)
        FilteredList<Costume> filteredData = new FilteredList<>(costumesData, p -> true);
        costumeSearchField
                .textProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            filteredData.setPredicate(
                                    costume -> {
                                        if (newValue == null || newValue.isEmpty()) return true;
                                        String lowerCaseFilter = newValue.toLowerCase();
                                        return costume.getName()
                                                .toLowerCase()
                                                .contains(lowerCaseFilter);
                                    });
                        });
        costumesTable.setItems(filteredData);
    }
}
