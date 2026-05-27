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

    private RentalFacade rentalFacade;

    @FXML
    public void initialize() {
        rentalFacade =
                new RentalFacade(
                        new RentalService(
                                new JdbcRentalRepository(), new JdbcRentalItemRepository()),
                        new JdbcRentalRepository(),
                        new JdbcCostumeRepository(),
                        new JdbcUserRepository());

        setupTable();
        loadDataAsync();
    }

    private void setupTable() {
        costumesCol.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                String.join(", ", data.getValue().getCostumeNames())));
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
                                    if ("ACTIVE".equals(status)) {
                                        badge.setText("Активна");
                                        badge.getStyleClass().addAll("badge", "accent");
                                    } else if ("COMPLETED".equals(status)) {
                                        badge.setText("Завершена");
                                        badge.getStyleClass().addAll("badge", "success");
                                    } else {
                                        badge.setText(status);
                                        badge.getStyleClass().add("badge");
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
