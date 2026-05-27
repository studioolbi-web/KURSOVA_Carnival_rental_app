package com.oliinyk.costumes.view;

import com.oliinyk.costumes.model.Category;
import com.oliinyk.costumes.model.Costume;
import com.oliinyk.costumes.repository.JdbcCategoryRepository;
import java.math.BigDecimal;
import java.util.UUID;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/** Контролер діалогового вікна для додавання/редагування костюма. */
public class CostumeDialogController {

    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField priceField;
    @FXML private TextField depositField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField imagePathField;

    private Costume costume;
    private boolean saveClicked = false;
    private final JdbcCategoryRepository categoryRepo = new JdbcCategoryRepository();
    private final com.oliinyk.costumes.service.ImageService imageService =
            new com.oliinyk.costumes.service.ImageService();

    @FXML
    public void initialize() {
        imagePathField.setEditable(false);
        imagePathField.setOnMouseClicked(
                e -> {
                    javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                    fileChooser.setTitle("Оберіть зображення");
                    fileChooser
                            .getExtensionFilters()
                            .addAll(
                                    new javafx.stage.FileChooser.ExtensionFilter(
                                            "Зображення", "*.png", "*.jpg", "*.jpeg"));

                    java.io.File file =
                            fileChooser.showOpenDialog(imagePathField.getScene().getWindow());
                    if (file != null) {
                        try {
                            String savedPath = imageService.saveImage(file);
                            imagePathField.setText(savedPath);
                        } catch (Exception ex) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Помилка");
                            alert.setHeaderText("Не вдалося зберегти зображення");
                            alert.setContentText(ex.getMessage());
                            alert.showAndWait();
                        }
                    }
                });

        categoryComboBox.setItems(FXCollections.observableArrayList(categoryRepo.findAll()));
        categoryComboBox.setConverter(
                new StringConverter<>() {
                    @Override
                    public String toString(Category category) {
                        return category == null ? "" : category.getName();
                    }

                    @Override
                    public Category fromString(String string) {
                        return null;
                    }
                });
    }

    public void setCostume(Costume costume) {
        this.costume = costume;
        if (costume != null) {
            nameField.setText(costume.getName());
            descriptionArea.setText(costume.getDescription());
            priceField.setText(String.format("%.2f", costume.getPricePerDay()).replace(',', '.'));
            depositField.setText(
                    String.format(
                                    "%.2f",
                                    costume.getDepositAmount() != null
                                            ? costume.getDepositAmount()
                                            : BigDecimal.ZERO)
                            .replace(',', '.'));
            imagePathField.setText(costume.getImagePath());

            categoryComboBox.getItems().stream()
                    .filter(c -> c.getId().equals(costume.getCategoryId()))
                    .findFirst()
                    .ifPresent(c -> categoryComboBox.setValue(c));
        }
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public Costume getCostume() {
        if (costume == null) {
            costume = Costume.builder().id(UUID.randomUUID()).build();
        }

        costume.setName(nameField.getText());
        costume.setDescription(descriptionArea.getText());
        costume.setPricePerDay(new BigDecimal(priceField.getText()));
        costume.setDepositAmount(new BigDecimal(depositField.getText()));
        costume.setImagePath(imagePathField.getText());
        if (categoryComboBox.getValue() != null) {
            costume.setCategoryId(categoryComboBox.getValue().getId());
        }

        return costume;
    }

    @FXML
    private void onSaveClicked() {
        if (validateInput()) {
            saveClicked = true;
            closeStage();
        }
    }

    @FXML
    private void onCancelClicked() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private boolean validateInput() {
        String errorMessage = "";

        if (nameField.getText() == null || nameField.getText().isEmpty()) {
            errorMessage += "Некоректна назва!\n";
        }
        if (priceField.getText() == null || priceField.getText().isEmpty()) {
            errorMessage += "Некоректна ціна!\n";
        } else {
            try {
                new BigDecimal(priceField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "Ціна повинна бути числом!\n";
            }
        }
        if (depositField.getText() == null || depositField.getText().isEmpty()) {
            errorMessage += "Некоректна застава!\n";
        } else {
            try {
                new BigDecimal(depositField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "Застава повинна бути числом!\n";
            }
        }
        if (categoryComboBox.getValue() == null) {
            errorMessage += "Оберіть категорію!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Помилка валідації");
            alert.setHeaderText("Будь ласка, виправте помилки");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
}
