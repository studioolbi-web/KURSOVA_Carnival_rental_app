package com.oliinyk.costumes.view;

import com.oliinyk.costumes.model.Costume;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class CostumeDetailController {

    @FXML private ImageView imageView;
    @FXML private Label nameLabel;
    @FXML private Label descLabel;
    @FXML private Label priceLabel;
    @FXML private Label depositLabel;

    @FXML
    public void setCostume(Costume costume) {
        nameLabel.setText(costume.getName());
        descLabel.setText(costume.getDescription());
        priceLabel.setText(String.format("Ціна оренди: %.2f грн/день", costume.getPricePerDay()));
        depositLabel.setText(String.format("Застава: %.2f грн", costume.getDepositAmount()));

        if (costume.getImagePath() != null && !costume.getImagePath().isEmpty()) {
            try {
                String path = costume.getImagePath();
                java.net.URL resourceUrl = getClass().getResource(path);
                if (resourceUrl != null) {
                    imageView.setImage(new Image(resourceUrl.toExternalForm(), true));
                } else {
                    java.io.File file = new java.io.File(path);
                    if (file.exists()) {
                        imageView.setImage(new Image(file.toURI().toString(), true));
                    }
                }
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

    @FXML
    private void closeDialog() {
        Stage stage = (Stage) nameLabel.getScene().getWindow();
        stage.close();
    }
}
