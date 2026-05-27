package com.oliinyk.costumes.view;

import com.oliinyk.costumes.dto.RentalDTO;
import com.oliinyk.costumes.repository.JdbcCostumeRepository;
import com.oliinyk.costumes.repository.JdbcRentalItemRepository;
import com.oliinyk.costumes.repository.JdbcRentalRepository;
import com.oliinyk.costumes.repository.JdbcUserRepository;
import com.oliinyk.costumes.service.RentalFacade;
import com.oliinyk.costumes.service.RentalService;
import com.oliinyk.costumes.service.SessionManager;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/** Контролер особистого кабінету користувача. Відображає історію оренд. */
public class MyRentalsController {

    @FXML private TableView<RentalDTO> rentalsTable;
    @FXML private TableColumn<RentalDTO, String> costumesCol;
    @FXML private TableColumn<RentalDTO, String> startCol;
    @FXML private TableColumn<RentalDTO, String> endCol;
    @FXML private TableColumn<RentalDTO, String> statusCol;
    @FXML private TableColumn<RentalDTO, String> totalCol;
    @FXML private javafx.scene.control.Label titleLabel;

    private RentalFacade rentalFacade;

    @FXML
    public void initialize() {
        rentalFacade =
                new RentalFacade(
                        new RentalService(
                                new JdbcRentalRepository(), new JdbcRentalItemRepository()),
                        new JdbcRentalRepository(),
                        com.oliinyk.costumes.repository.RepositoryProvider.getCostumeRepository(),
                        new JdbcUserRepository());

        setupTable();
        loadDataAsync();
    }

    private void setupTable() {
        titleLabel.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("rentals.title"));
        costumesCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("rentals.costumes"));
        startCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("rentals.start_date"));
        endCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("rentals.end_date"));
        statusCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("rentals.status"));
        totalCol.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("rentals.total"));

        costumesCol.setCellValueFactory(
                data -> new SimpleStringProperty("")); // Cell value is not used since we use graphic
        
        costumesCol.setCellFactory(
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
                                javafx.scene.layout.VBox vBox = new javafx.scene.layout.VBox(8);
                                vBox.setPadding(new javafx.geometry.Insets(8, 0, 8, 0));
                                
                                for (int i = 0; i < rental.getCostumeNames().size(); i++) {
                                    String name = rental.getCostumeNames().get(i);
                                    String imagePath = rental.getCostumeImages() != null && i < rental.getCostumeImages().size() ? rental.getCostumeImages().get(i) : null;
                                    
                                    javafx.scene.layout.HBox hBox = new javafx.scene.layout.HBox(12);
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
                                    imageView.setFitWidth(45);
                                    imageView.setFitHeight(45);
                                    imageView.setPreserveRatio(true);
                                    
                                    javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(name);
                                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: -color-fg-default;");
                                    
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
        startCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getStartDate().toString()));
        endCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getEndDate().toString()));

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
                                            badge.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("rentals.status.reserved"));
                                            badge.getStyleClass().addAll("badge", "accent");
                                        }
                                        case "ISSUED", "ACTIVE" -> {
                                            badge.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("rentals.status.active"));
                                            badge.getStyleClass().addAll("badge", "warning");
                                        }
                                        case "OVERDUE" -> {
                                            badge.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("rentals.status.overdue"));
                                            badge.getStyleClass().addAll("badge", "danger");
                                        }
                                        case "RETURNED", "COMPLETED" -> {
                                            badge.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("rentals.status.completed"));
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

        totalCol.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                String.format("%.2f грн", data.getValue().getTotalPrice())));
    }

    private void loadDataAsync() {
        com.oliinyk.costumes.model.User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        Task<List<RentalDTO>> task =
                new Task<>() {
                    @Override
                    protected List<RentalDTO> call() throws Exception {
                        return rentalFacade.getRentalsByUserId(user.getId());
                    }
                };

        task.setOnSucceeded(
                e -> rentalsTable.setItems(FXCollections.observableArrayList(task.getValue())));
        new Thread(task).start();
    }
}
