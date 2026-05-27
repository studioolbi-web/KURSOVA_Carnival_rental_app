package com.oliinyk.costumes.view;

import com.oliinyk.costumes.repository.JdbcCostumeRepository;
import com.oliinyk.costumes.repository.JdbcRentalItemRepository;
import com.oliinyk.costumes.repository.JdbcRentalRepository;
import com.oliinyk.costumes.repository.JdbcUserRepository;
import com.oliinyk.costumes.service.RentalFacade;
import com.oliinyk.costumes.service.RentalService;
import com.oliinyk.costumes.viewmodel.BasketViewModel;
import com.oliinyk.costumes.viewmodel.CatalogViewModel;
import com.oliinyk.costumes.viewmodel.SettingsViewModel;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController {

    @FXML private StackPane contentArea;

    // Створюємо загальний ViewModel для каталогу, щоб не втрачати стан при перемиканні
    @FXML private javafx.scene.control.Button btnAdmin;
    @FXML private javafx.scene.control.Button btnMyRentals;

    private final CatalogViewModel catalogViewModel = new CatalogViewModel();
    private BasketViewModel basketViewModel;
    private final SettingsViewModel settingsViewModel = new SettingsViewModel();
    private RentalFacade rentalFacade;

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

        basketViewModel = new BasketViewModel(rentalFacade);

        // Ховаємо кнопки залежно від ролі (Вимога розділу 2.2)
        com.oliinyk.costumes.model.User user =
                com.oliinyk.costumes.service.SessionManager.getInstance().getCurrentUser();

        boolean isAdmin = user != null && "ADMIN".equals(user.getRole());

        btnAdmin.setVisible(isAdmin);
        btnAdmin.setManaged(isAdmin);

        // "Мої оренди" доступні всім ролям (Вимога UI/UX)
        btnMyRentals.setVisible(true);
        btnMyRentals.setManaged(true);

        showCatalog(); // Відкриваємо каталог при старті
    }

    @FXML
    private void showCatalog() {
        catalogViewModel.loadFromDatabase(); // Оновлюємо дані перед показом
        loadView("/views/CatalogView.fxml", catalogViewModel);
    }

    @FXML
    private void showBasket() {
        basketViewModel.refresh();
        loadView("/views/BasketView.fxml", basketViewModel);
    }

    @FXML
    private void showMyRentals() {
        loadView("/views/MyRentalsView.fxml", null);
    }

    @FXML
    private void showSettings() {
        loadView("/views/SettingsView.fxml", settingsViewModel);
    }

    @FXML
    private void showAdminPanel() {
        loadView("/views/AdminPanelView.fxml", null);
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 400, 350));

            // Візуальний баг 2: Розмір вікна при Logout (Вимога UX)
            stage.setMaximized(false);
            stage.setWidth(600);
            stage.setHeight(500);
            stage.setMinWidth(600);
            stage.setMinHeight(500);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath, Object viewModel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof CatalogController) {
                ((CatalogController) controller).setViewModel((CatalogViewModel) viewModel);
            } else if (controller instanceof BasketController) {
                ((BasketController) controller).setViewModel((BasketViewModel) viewModel);
            } else if (controller instanceof SettingsController) {
                ((SettingsController) controller).setViewModel((SettingsViewModel) viewModel);
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
