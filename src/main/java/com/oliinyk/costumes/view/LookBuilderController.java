package com.oliinyk.costumes.view;

import com.oliinyk.costumes.model.Costume;
import com.oliinyk.costumes.model.CustomLook;
import com.oliinyk.costumes.repository.CustomLookRepository;
import com.oliinyk.costumes.repository.RepositoryProvider;
import com.oliinyk.costumes.service.BasketService;
import com.oliinyk.costumes.service.SessionManager;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LookBuilderController {

    @FXML private TilePane itemsPalette;
    @FXML private StackPane canvasContainer;
    @FXML private ImageView backgroundImage;
    @FXML private Pane canvasPane;
    @FXML private Label totalPriceLabel;

    private final CustomLookRepository customLookRepo = RepositoryProvider.getCustomLookRepository();
    private List<Costume> addedItems = new ArrayList<>();
    private BigDecimal totalPrice = BigDecimal.ZERO;

    private double dragDeltaX, dragDeltaY;

    @FXML
    public void initialize() {
        loadPaletteItems();
        
        // Обрізаємо вміст canvasPane, щоб зображення не виходили за його межі при масштабуванні
        javafx.scene.shape.Rectangle clipRect = new javafx.scene.shape.Rectangle();
        clipRect.widthProperty().bind(canvasPane.widthProperty());
        clipRect.heightProperty().bind(canvasPane.heightProperty());
        canvasPane.setClip(clipRect);
    }

    private void loadPaletteItems() {
        // Завантажуємо лише елементи образу (категорія 'Елементи образу')
        // ID категорії елементів образу з міграції V11
        UUID elementsCategoryId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        
        List<Costume> allCostumes = RepositoryProvider.getCostumeRepository().findAll();
        List<Costume> elements = allCostumes.stream()
                .filter(c -> elementsCategoryId.equals(c.getCategoryId()))
                .collect(Collectors.toList());

        for (Costume costume : elements) {
            itemsPalette.getChildren().add(createPaletteItem(costume));
        }
    }

    private VBox createPaletteItem(Costume costume) {
        VBox card = new VBox(5);
        card.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-alignment: center; -fx-cursor: hand;");
        
        ImageView imageView = new ImageView();
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);

        if (costume.getImagePath() != null && !costume.getImagePath().isEmpty()) {
            try {
                java.net.URL resourceUrl = getClass().getResource(costume.getImagePath());
                if (resourceUrl != null) {
                    imageView.setImage(new Image(resourceUrl.toExternalForm(), true));
                } else {
                    File file = new File(costume.getImagePath());
                    if (file.exists()) {
                        imageView.setImage(new Image(file.toURI().toString(), true));
                    }
                }
            } catch (Exception ignored) {}
        }

        Label nameLabel = new Label(costume.getName());
        nameLabel.setStyle("-fx-font-size: 12;");
        
        card.getChildren().addAll(imageView, nameLabel);
        
        // Додавання на полотно при кліку
        card.setOnMouseClicked(e -> addItemToCanvas(costume, imageView.getImage()));
        
        return card;
    }

    private void addItemToCanvas(Costume costume, Image image) {
        if (image == null) return;

        ImageView canvasItem = new ImageView(image);
        canvasItem.setFitWidth(150);
        canvasItem.setPreserveRatio(true);
        canvasItem.setLayoutX(100);
        canvasItem.setLayoutY(100);

        // Drag and Drop логіка
        canvasItem.setOnMousePressed((MouseEvent event) -> {
            dragDeltaX = canvasItem.getLayoutX() - event.getSceneX();
            dragDeltaY = canvasItem.getLayoutY() - event.getSceneY();
            canvasItem.toFront(); // Вивести на передній план
        });

        canvasItem.setOnMouseDragged((MouseEvent event) -> {
            double newX = event.getSceneX() + dragDeltaX;
            double newY = event.getSceneY() + dragDeltaY;
            
            // Запобігаємо виходу за межі полотна
            double maxX = canvasPane.getWidth() - canvasItem.getBoundsInParent().getWidth();
            double maxY = canvasPane.getHeight() - canvasItem.getBoundsInParent().getHeight();
            
            if (newX < 0) newX = 0;
            if (newY < 0) newY = 0;
            if (newX > maxX && maxX > 0) newX = maxX;
            if (newY > maxY && maxY > 0) newY = maxY;

            canvasItem.setLayoutX(newX);
            canvasItem.setLayoutY(newY);
        });
        
        // Масштабування елементу коліщатком миші
        canvasItem.setOnScroll((javafx.scene.input.ScrollEvent event) -> {
            double zoomFactor = 1.05;
            if (event.getDeltaY() < 0) {
                zoomFactor = 1 / zoomFactor;
            }
            
            // Обмежуємо масштабування
            double newScaleX = canvasItem.getScaleX() * zoomFactor;
            double newScaleY = canvasItem.getScaleY() * zoomFactor;
            if (newScaleX > 0.2 && newScaleX < 5.0) {
                canvasItem.setScaleX(newScaleX);
                canvasItem.setScaleY(newScaleY);
            }
            event.consume();
        });
        
        // Видалення елементу правим кліком
        canvasItem.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                canvasPane.getChildren().remove(canvasItem);
                addedItems.remove(costume);
                updateTotalPrice();
            }
        });

        canvasPane.getChildren().add(canvasItem);
        addedItems.add(costume);
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        totalPrice = addedItems.stream()
                .map(Costume::getPricePerDay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalPriceLabel.setText(String.format("%.2f ₴", totalPrice));
    }

    @FXML
    private void uploadBackground() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Виберіть фонове зображення");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(canvasContainer.getScene().getWindow());
        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());
            backgroundImage.setImage(image);
            backgroundImage.setFitWidth(canvasContainer.getWidth());
            backgroundImage.setFitHeight(canvasContainer.getHeight());
        }
    }

    @FXML
    private void clearCanvas() {
        canvasPane.getChildren().clear();
        backgroundImage.setImage(null);
        addedItems.clear();
        updateTotalPrice();
    }

    @FXML
    private void saveLookImage() {
        if (addedItems.isEmpty()) {
            showAlert("Помилка", "Додайте хоча б один елемент на полотно.");
            return;
        }

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage snapshot = canvasContainer.snapshot(params, null);
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Зберегти образ");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        fileChooser.setInitialFileName("my_custom_look.png");
        
        File file = fileChooser.showSaveDialog(canvasContainer.getScene().getWindow());
        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
                
                // Зберігаємо в БД (опціонально)
                if (SessionManager.getInstance().isLoggedIn()) {
                    CustomLook look = CustomLook.builder()
                            .id(UUID.randomUUID())
                            .userId(SessionManager.getInstance().getCurrentUser().getId())
                            .name("Custom Look " + LocalDateTime.now().getSecond())
                            .imagePath(file.getAbsolutePath())
                            .totalPrice(totalPrice)
                            .items(new ArrayList<>(addedItems))
                            .createdAt(LocalDateTime.now())
                            .build();
                    customLookRepo.save(look);
                }
                
                showAlert("Успіх", "Ваш унікальний образ успішно збережено!");
            } catch (IOException e) {
                showAlert("Помилка", "Не вдалося зберегти зображення: " + e.getMessage());
            }
        }
    }

    @FXML
    private void rentLook() {
        if (addedItems.isEmpty()) {
            showAlert("Помилка", "Додайте хоча б один елемент на полотно.");
            return;
        }
        
        for (Costume costume : addedItems) {
            BasketService.getInstance().addItem(costume);
        }
        
        showAlert("Успіх", "Елементи вашого образу додано до кошика!");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
