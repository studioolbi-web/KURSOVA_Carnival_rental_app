package com.oliinyk.costumes;

import atlantafx.base.theme.PrimerLight;
import com.oliinyk.costumes.infrastructure.DatabaseManager;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Головний клас додатку. Ініціалізує базу даних та запускає JavaFX інтерфейс. */
public class Main extends Application {

    @Override
    public void init() throws Exception {
        // 1. Ініціалізація бази даних та запуск міграцій Flyway
        // H2 embedded database file in current directory
        DatabaseManager.initialize("jdbc:h2:./carnival_rental_db;DB_CLOSE_DELAY=-1", "sa", "");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 2. Налаштування теми AtlantaFX (Світла за замовчуванням)
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        // 3. Завантаження головного FXML
        URL fxmlLocation = getClass().getResource("/views/LoginView.fxml");
        if (fxmlLocation == null) {
            throw new IllegalStateException(
                    "Не вдалося знайти файл /views/LoginView.fxml у ресурсах");
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        // 4. Відображення головного вікна
        primaryStage.setTitle("Карнавальні Костюми - Авторизація");
        primaryStage.setScene(new Scene(root, 450, 400));
        primaryStage.setMinWidth(450);
        primaryStage.setMinHeight(400);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // Завершення роботи: безпечне закриття з'єднань із базою даних
        DatabaseManager.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
