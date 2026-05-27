package com.oliinyk.costumes.view;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private VBox registerPane;
    @FXML private VBox verifyPane;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField codeField;
    @FXML private Label errorLabel;

    private com.oliinyk.costumes.viewmodel.RegisterViewModel viewModel;

    @FXML
    public void initialize() {
        com.oliinyk.costumes.service.AuthService authService =
                new com.oliinyk.costumes.service.AuthService(
                        new com.oliinyk.costumes.repository.JdbcUserRepository(),
                        new com.oliinyk.costumes.service.ConsoleEmailServiceImpl());

        viewModel = new com.oliinyk.costumes.viewmodel.RegisterViewModel(authService);

        // Біндінг властивостей
        emailField.textProperty().bindBidirectional(viewModel.emailProperty());
        passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
        codeField.textProperty().bindBidirectional(viewModel.codeProperty());
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());

        // Реакція на успішну реєстрацію
        viewModel
                .registrationSuccessfulProperty()
                .addListener(
                        (obs, old, success) -> {
                            if (success) {
                                registerPane.setVisible(false);
                                registerPane.setManaged(false);
                                verifyPane.setVisible(true);
                                verifyPane.setManaged(true);
                            }
                        });

        // Реакція на успішну верифікацію
        viewModel
                .verificationSuccessfulProperty()
                .addListener(
                        (obs, old, success) -> {
                            if (success) {
                                errorLabel.setStyle("-fx-text-fill: -color-success-fg;");
                                verifyPane.setVisible(false);
                                verifyPane.setManaged(false);
                                javafx.scene.control.Button backBtn =
                                        new javafx.scene.control.Button("Повернутися до входу");
                                backBtn.setOnAction(e -> navigateBack());
                                backBtn.getStyleClass().add("accent");
                                ((VBox) errorLabel.getParent()).getChildren().add(backBtn);
                            }
                        });
    }

    @FXML
    private void handleRegister() {
        viewModel.attemptRegister();
    }

    @FXML
    private void handleVerify() {
        viewModel.attemptVerify();
    }

    @FXML
    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 350));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
