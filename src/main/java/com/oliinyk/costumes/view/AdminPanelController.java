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
    
    @FXML private javafx.scene.control.Label titleLabel;
    @FXML private javafx.scene.control.Tab dashboardTab;
    @FXML private javafx.scene.control.Tab costumesTab;
    @FXML private javafx.scene.control.Tab usersTab;
    @FXML private javafx.scene.control.Tab rentalsTab;
    @FXML private javafx.scene.control.Label analyticsLabel;
    @FXML private javafx.scene.control.Label statusChartLabel;
    @FXML private javafx.scene.control.Label topCostumesLabel;
    @FXML private javafx.scene.chart.CategoryAxis topCostumesXAxis;
    @FXML private javafx.scene.chart.NumberAxis topCostumesYAxis;
    @FXML private javafx.scene.control.Button addCostumeBtn;
    @FXML private javafx.scene.control.Button backupBtn;
    @FXML private javafx.scene.control.Button restoreBtn;
    @FXML private javafx.scene.control.Button exportCsvBtn;
    @FXML private javafx.scene.control.TableColumn<Costume, String> costumesNameCol;
    @FXML private javafx.scene.control.TableColumn<Costume, String> costumesDescCol;
    @FXML private javafx.scene.control.TableColumn<Costume, String> costumesPriceCol;
    @FXML private javafx.scene.control.TableColumn<Costume, Void> costumesActionsCol;
    @FXML private javafx.scene.control.TableColumn<User, String> usersEmailCol;
    @FXML private javafx.scene.control.TableColumn<User, String> usersRoleCol;
    @FXML private javafx.scene.control.TableColumn<User, String> usersVerifiedCol;
    @FXML private javafx.scene.control.TableColumn<RentalDTO, String> rentalsUserCol;
    @FXML private javafx.scene.control.TableColumn<RentalDTO, String> rentalsCostumesCol;
    @FXML private javafx.scene.control.TableColumn<RentalDTO, String> rentalsPeriodCol;
    @FXML private javafx.scene.control.TableColumn<RentalDTO, String> rentalsStatusCol;
    @FXML private javafx.scene.control.TableColumn<RentalDTO, String> rentalsTotalCol;
    @FXML private javafx.scene.control.TableColumn<RentalDTO, Void> rentalsActionCol;
    @FXML private javafx.scene.control.TableColumn<RentalDTO, Void> rentalsDeleteCol;
    @FXML private TextField rentalsSearchField;
    @FXML private TextField usersSearchField;

    private final com.oliinyk.costumes.repository.CostumeRepository costumeRepo = com.oliinyk.costumes.repository.RepositoryProvider.getCostumeRepository();
    private final JdbcUserRepository userRepo = new JdbcUserRepository();
    private final com.oliinyk.costumes.service.ReportService reportService =
            new com.oliinyk.costumes.service.ReportService();
    private final com.oliinyk.costumes.service.BackupService backupService =
            new com.oliinyk.costumes.service.BackupService();
    private RentalFacade rentalFacade;

    private ObservableList<Costume> costumesData = FXCollections.observableArrayList();
    private ObservableList<RentalDTO> rentalsData = FXCollections.observableArrayList();
    private ObservableList<User> usersData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Ініціалізація фасаду
        rentalFacade =
                new RentalFacade(
                        new RentalService(
                                new JdbcRentalRepository(), new JdbcRentalItemRepository()),
                        new JdbcRentalRepository(),
                        com.oliinyk.costumes.repository.RepositoryProvider.getCostumeRepository(),
                        new JdbcUserRepository());

        setupTables();
        setupSearch();
        setupI18n();
        loadDataAsync();

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

    private void setupI18n() {
        titleLabel.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.title"));
        dashboardTab.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.tab.dashboard"));
        costumesTab.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.tab.costumes"));
        usersTab.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.tab.users"));
        rentalsTab.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.tab.rentals"));
        
        analyticsLabel.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.analytics"));
        statusChartLabel.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.status_chart"));
        topCostumesLabel.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.top_costumes"));
        topCostumesXAxis.labelProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.chart.costume"));
        topCostumesYAxis.labelProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.chart.rentals_count"));
        
        addCostumeBtn.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.btn.add_costume"));
        backupBtn.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.btn.backup"));
        restoreBtn.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.btn.restore"));
        exportCsvBtn.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.btn.export_csv"));
        costumeSearchField.promptTextProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.search_prompt"));
        
        costumesNameCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.col.name"));
        costumesDescCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.col.desc"));
        costumesPriceCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.col.price"));
        costumesActionsCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.col.actions"));
        
        usersEmailCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.col.email"));
        usersRoleCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.col.role"));
        usersVerifiedCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.col.verified"));
        
        rentalsUserCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.col.user"));
        rentalsCostumesCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.col.costumes"));
        rentalsPeriodCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.col.period"));
        rentalsStatusCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.col.status"));
        rentalsTotalCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.col.total"));
        rentalsActionCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.col.actions"));
        rentalsSearchField.promptTextProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.search_email_prompt"));
        usersSearchField.promptTextProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.search_email_prompt"));
    }

    private void setupCostumesTable() {
        costumesNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        costumesDescCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        costumesPriceCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f грн", data.getValue().getPricePerDay())));
        costumesActionsCol.setCellFactory(
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
            dialogStage.titleProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding(costume == null ? "dialog.title.add" : "dialog.title.edit"));
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

    @FXML
    private void onBackupCostumesClicked() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Зберегти Backup");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JSON файли", "*.json"));
        fileChooser.setInitialFileName("costumes_backup_" + java.time.LocalDate.now() + ".json");
        java.io.File file = fileChooser.showSaveDialog(costumesTable.getScene().getWindow());

        if (file != null) {
            try {
                backupService.exportCostumes(costumeRepo.findAll(), file);
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Успіх");
                alert.setHeaderText(null);
                alert.setContentText("Backup успішно збережено у файл.");
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Помилка");
                alert.setHeaderText("Не вдалося зберегти backup");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void onRestoreCostumesClicked() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Вибрати Backup для відновлення");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JSON файли", "*.json"));
        java.io.File file = fileChooser.showOpenDialog(costumesTable.getScene().getWindow());

        if (file != null) {
            try {
                List<Costume> restored = backupService.importCostumes(file);
                for (Costume c : restored) {
                    // Якщо костюм з таким ID існує - оновлюємо, інакше - додаємо
                    if (costumeRepo.findById(c.getId()).isPresent()) {
                        costumeRepo.update(c);
                    } else {
                        costumeRepo.save(c);
                    }
                }
                loadDataAsync();
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Успіх");
                alert.setHeaderText(null);
                alert.setContentText("Дані успішно відновлено!");
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Помилка");
                alert.setHeaderText("Не вдалося відновити дані");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
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
        usersEmailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        usersRoleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
        usersVerifiedCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isVerified() ? com.oliinyk.costumes.util.I18nManager.get("admin.yes") : com.oliinyk.costumes.util.I18nManager.get("admin.no")));
        usersVerifiedCol.setCellFactory(
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

        TableColumn<User, String> blockedCol = new TableColumn<>();
        blockedCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.col.blocked"));
        blockedCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isBlocked() ? com.oliinyk.costumes.util.I18nManager.get("admin.yes") : com.oliinyk.costumes.util.I18nManager.get("admin.no")));
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

        TableColumn<User, Void> actionCol = new TableColumn<>();
        actionCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("admin.col.actions"));
        actionCol.setCellFactory(
                param ->
                        new javafx.scene.control.TableCell<>() {
                            private final javafx.scene.control.Button blockBtn = new javafx.scene.control.Button();
                            private final javafx.scene.control.Button deleteBtn = new javafx.scene.control.Button();
                            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(8, blockBtn, deleteBtn);

                            {
                                pane.setAlignment(javafx.geometry.Pos.CENTER);
                                blockBtn.getStyleClass().add("button-icon");
                                blockBtn.setOnAction(event -> {
                                    User user = getTableView().getItems().get(getIndex());
                                    handleToggleBlock(user);
                                });
                                
                                deleteBtn.getStyleClass().addAll("button-icon", "flat", "danger");
                                deleteBtn.setGraphic(new org.kordamp.ikonli.javafx.FontIcon("fas-trash"));
                                deleteBtn.setTooltip(new javafx.scene.control.Tooltip("Видалити користувача"));
                                deleteBtn.setOnAction(event -> {
                                    User user = getTableView().getItems().get(getIndex());
                                    handleDeleteUser(user);
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
                                    setGraphic(pane);
                                }
                            }
                        });

        usersTable.getColumns().addAll(blockedCol, actionCol);
    }

    private void handleToggleBlock(User user) {
        user.setBlocked(!user.isBlocked());
        userRepo.update(user);
        loadDataAsync();
    }

    private void handleDeleteUser(User user) {
        if ("ADMIN".equals(user.getRole())) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Помилка");
            alert.setHeaderText("Неможливо видалити адміністратора");
            alert.setContentText("У системі має залишатися хоча б один адміністратор.");
            alert.showAndWait();
            return;
        }

        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Підтвердження");
        confirm.setHeaderText("Видалити користувача " + user.getEmail() + "?");
        confirm.setContentText("Увага: користувача з існуючими орендами неможливо видалити через захист бази даних.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    userRepo.delete(user.getId());
                    loadDataAsync();
                } catch (Exception e) {
                    javafx.scene.control.Alert err = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    err.setTitle("Помилка видалення");
                    err.setHeaderText("Неможливо видалити користувача");
                    err.setContentText("Цей користувач має історію замовлень.\nБаза даних блокує видалення для збереження цілісності фінансової історії.\n\n(Помилка: " + e.getMessage() + ")");
                    err.showAndWait();
                }
            }
        });
    }

    private void setupRentalsTable() {
        rentalsUserCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getUserEmail()));
        
        rentalsPeriodCol.setCellValueFactory(
                data -> {
                    java.time.LocalDate start = data.getValue().getStartDate();
                    java.time.LocalDate end = data.getValue().getEndDate();
                    long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                    if (days == 0) days = 1;
                    return new SimpleStringProperty(start + " → " + end + " (" + days + " дн.)");
                });

        rentalsCostumesCol.setCellValueFactory(data -> new SimpleStringProperty(""));
        rentalsCostumesCol.setCellFactory(
                column -> new javafx.scene.control.TableCell<>() {
                    @Override
                    protected void updateItem(String val, boolean empty) {
                        super.updateItem(val, empty);
                        if (empty || getTableRow() == null || getTableView().getItems().isEmpty()) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            int index = getIndex();
                            if (index >= 0 && index < getTableView().getItems().size()) {
                                RentalDTO rental = getTableView().getItems().get(index);
                                javafx.scene.layout.VBox vBox = new javafx.scene.layout.VBox(4);
                                vBox.setPadding(new javafx.geometry.Insets(4, 0, 4, 0));
                                
                                for (int i = 0; i < rental.getCostumeNames().size(); i++) {
                                    String name = rental.getCostumeNames().get(i);
                                    String imagePath = rental.getCostumeImages() != null && i < rental.getCostumeImages().size() ? rental.getCostumeImages().get(i) : null;
                                    
                                    javafx.scene.layout.HBox hBox = new javafx.scene.layout.HBox(8);
                                    hBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                                    
                                    javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
                                    if (imagePath != null && !imagePath.isEmpty()) {
                                        try {
                                            java.net.URL resourceUrl = getClass().getResource(imagePath);
                                            if (resourceUrl != null) {
                                                imageView.setImage(new javafx.scene.image.Image(resourceUrl.toExternalForm(), true));
                                            } else {
                                                java.io.File file = new java.io.File(imagePath);
                                                if (file.exists()) {
                                                    imageView.setImage(new javafx.scene.image.Image(file.toURI().toString(), true));
                                                }
                                            }
                                        } catch (Exception ex) {}
                                    }
                                    imageView.setFitWidth(30);
                                    imageView.setFitHeight(30);
                                    imageView.setPreserveRatio(true);
                                    
                                    javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(name);
                                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: -color-fg-default;");
                                    
                                    hBox.getChildren().addAll(imageView, nameLabel);
                                    vBox.getChildren().add(hBox);
                                }
                                setText(null);
                                setGraphic(vBox);
                            } else {
                                setText(null);
                                setGraphic(null);
                            }
                        }
                    }
                });

        rentalsStatusCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getStatus()));
        rentalsStatusCol.setCellFactory(
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
                                            badge.setText(com.oliinyk.costumes.util.I18nManager.get("rentals.status.reserved"));
                                            badge.getStyleClass().addAll("badge", "accent");
                                        }
                                        case "ISSUED", "ACTIVE" -> {
                                            badge.setText(com.oliinyk.costumes.util.I18nManager.get("rentals.status.issued"));
                                            badge.getStyleClass().addAll("badge", "warning");
                                        }
                                        case "OVERDUE" -> {
                                            badge.setText(com.oliinyk.costumes.util.I18nManager.get("rentals.status.overdue"));
                                            badge.getStyleClass().addAll("badge", "danger");
                                            getTableRow().setStyle("-fx-background-color: rgba(255, 0, 0, 0.05);");
                                        }
                                        case "RETURNED", "COMPLETED" -> {
                                            badge.setText(com.oliinyk.costumes.util.I18nManager.get("rentals.status.returned"));
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

        rentalsTotalCol.setCellValueFactory(
                data -> {
                    java.math.BigDecimal total = data.getValue().getTotalPrice();
                    java.math.BigDecimal penalty = data.getValue().getPenaltyAmount();
                    String text = String.format("%.2f грн", total);
                    if (penalty.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        text += "\n+" + String.format("%.2f грн", penalty) + " (Штраф)";
                    }
                    return new SimpleStringProperty(text);
                });

        rentalsActionCol.setCellFactory(
                param ->
                        new javafx.scene.control.TableCell<>() {
                            private final javafx.scene.control.Button actionBtn = new javafx.scene.control.Button();
                            
                            {
                                setAlignment(javafx.geometry.Pos.CENTER);
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || getTableRow() == null) {
                                    setGraphic(null);
                                } else {
                                    int index = getIndex();
                                    if (index >= 0 && index < getTableView().getItems().size()) {
                                        RentalDTO rental = getTableView().getItems().get(index);
                                        
                                        actionBtn.setOnAction(null);
                                        actionBtn.getStyleClass().removeAll("accent", "success");
                                        
                                        if ("RESERVED".equals(rental.getStatus())) {
                                            actionBtn.setText("Видати");
                                            actionBtn.getStyleClass().add("accent");
                                            actionBtn.setVisible(true);
                                            actionBtn.setOnAction(e -> handleStatusChange(rental, "ISSUED"));
                                        } else if ("ISSUED".equals(rental.getStatus()) || "ACTIVE".equals(rental.getStatus()) || "OVERDUE".equals(rental.getStatus())) {
                                            actionBtn.setText("Прийняти");
                                            actionBtn.getStyleClass().add("success");
                                            actionBtn.setVisible(true);
                                            actionBtn.setOnAction(e -> handleStatusChange(rental, "RETURNED"));
                                        } else {
                                            actionBtn.setVisible(false);
                                        }
                                        
                                        setGraphic(actionBtn);
                                    } else {
                                        setGraphic(null);
                                    }
                                }
                            }
                        });

        rentalsDeleteCol.setCellFactory(
                param ->
                        new javafx.scene.control.TableCell<>() {
                            private final javafx.scene.control.Button deleteBtn = new javafx.scene.control.Button();

                            {
                                setAlignment(javafx.geometry.Pos.CENTER);
                                deleteBtn.getStyleClass().addAll("button-icon", "flat", "danger");
                                deleteBtn.setGraphic(new org.kordamp.ikonli.javafx.FontIcon("fas-trash"));
                                deleteBtn.setTooltip(new javafx.scene.control.Tooltip("Видалити"));
                                deleteBtn.setOnAction(event -> {
                                    RentalDTO rental = getTableView().getItems().get(getIndex());
                                    handleDeleteRental(rental);
                                });
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || getTableRow() == null) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(deleteBtn);
                                }
                            }
                        });
    }

    private void handleDeleteRental(RentalDTO rental) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження");
        alert.setHeaderText("Видалити цю оренду?");
        alert.setContentText("Увага: це видалить запис з бази даних.");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    rentalFacade.deleteRental(rental.getId());
                    loadDataAsync();
                } catch (Exception e) {
                    javafx.scene.control.Alert err = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    err.setTitle("Помилка");
                    err.setHeaderText("Неможливо видалити оренду");
                    err.setContentText(e.getMessage());
                    err.showAndWait();
                }
            }
        });
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
                    String localizedKey =
                            switch (status) {
                                case "RESERVED" -> "rentals.status.reserved";
                                case "ISSUED" -> "rentals.status.issued";
                                case "RETURNED" -> "rentals.status.returned";
                                case "COMPLETED" -> "rentals.status.completed";
                                case "ACTIVE" -> "rentals.status.active";
                                case "OVERDUE" -> "rentals.status.overdue";
                                default -> null;
                            };
                    String localizedStatus = localizedKey != null ? com.oliinyk.costumes.util.I18nManager.get(localizedKey) : status;
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
                                    usersData.setAll(users);
                                    rentalsData.setAll(rentals);
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
        FilteredList<Costume> filteredCostumes = new FilteredList<>(costumesData, p -> true);
        costumeSearchField
                .textProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            filteredCostumes.setPredicate(
                                    costume -> {
                                        if (newValue == null || newValue.isEmpty()) return true;
                                        String lowerCaseFilter = newValue.toLowerCase();
                                        return costume.getName()
                                                .toLowerCase()
                                                .contains(lowerCaseFilter);
                                    });
                        });
        costumesTable.setItems(filteredCostumes);
        
        FilteredList<RentalDTO> filteredRentals = new FilteredList<>(rentalsData, p -> true);
        rentalsSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredRentals.setPredicate(rental -> {
                if (newValue == null || newValue.isEmpty()) return true;
                return rental.getUserEmail().toLowerCase().contains(newValue.toLowerCase());
            });
        });
        rentalsTable.setItems(filteredRentals);

        FilteredList<User> filteredUsers = new FilteredList<>(usersData, p -> true);
        usersSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) return true;
                return user.getEmail().toLowerCase().contains(newValue.toLowerCase());
            });
        });
        usersTable.setItems(filteredUsers);
    }
}
