package com.oliinyk.costumes.view;

import atlantafx.base.theme.PrimerLight;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private com.oliinyk.costumes.viewmodel.LoginViewModel viewModel;

    @FXML
    public void initialize() {
        com.oliinyk.costumes.service.AuthService authService =
                new com.oliinyk.costumes.service.AuthService(
                        new com.oliinyk.costumes.repository.JdbcUserRepository(),
                        new com.oliinyk.costumes.service.SmtpEmailServiceImpl());

        viewModel = new com.oliinyk.costumes.viewmodel.LoginViewModel(authService);

        // Біндінг властивостей (Вимога 4.4.4)
        emailField.textProperty().bindBidirectional(viewModel.emailProperty());
        passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());

        loginButton.setOnAction(e -> handleLogin());

        // Реакція на успішний вхід
        viewModel
                .loginSuccessfulProperty()
                .addListener(
                        (obs, old, success) -> {
                            if (success) {
                                navigateToCatalog();
                            }
                        });
    }

    private void handleLogin() {
        viewModel.attemptLogin();
    }

    private void navigateToCatalog() {
        try {
            URL fxmlLocation = getClass().getResource("/views/MainView.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("Не знайдено /views/MainView.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Застосування теми за замовчуванням
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

            // Заміна сцени на тому ж Stage
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));

            // Візуальний баг 3: Стиснутий Каталог по замовчуванню (Вимога UX)
            stage.setResizable(true);
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.setTitle("Карнавальні Костюми");
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Помилка відкриття каталогу: " + e.getMessage());
        }
    }

    @FXML
    private void navigateToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegisterView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 450, 400));
            stage.setTitle("Реєстрація");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Помилка відкриття форми: " + e.getMessage());
        }
    }
}
