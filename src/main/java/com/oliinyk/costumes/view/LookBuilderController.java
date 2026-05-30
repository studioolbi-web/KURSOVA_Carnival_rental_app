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
    @FXML private javafx.scene.control.ComboBox<String> categoryFilter;

    private final CustomLookRepository customLookRepo = RepositoryProvider.getCustomLookRepository();
    private List<Costume> addedItems = new ArrayList<>();
    private BigDecimal totalPrice = BigDecimal.ZERO;

    private double dragDeltaX, dragDeltaY;

    @FXML
    public void initialize() {
        categoryFilter.getItems().addAll("Усі", "Топи", "Штани", "Спідниці", "Капелюхи", "Маски", "Аксесуари");
        categoryFilter.setValue("Усі");
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadPaletteItems(newVal));
        
        loadPaletteItems("Усі");
        
        // Обрізаємо вміст canvasPane, щоб зображення не виходили за його межі при масштабуванні
        javafx.scene.shape.Rectangle clipRect = new javafx.scene.shape.Rectangle();
        clipRect.widthProperty().bind(canvasPane.widthProperty());
        clipRect.heightProperty().bind(canvasPane.heightProperty());
        canvasPane.setClip(clipRect);
        
        // Пропускаємо кліки крізь порожні місця canvasPane до backgroundImage
        canvasPane.setPickOnBounds(false);
    }

    private void loadPaletteItems(String filter) {
        itemsPalette.getChildren().clear();
        // Завантажуємо лише елементи образу (категорія 'Елементи образу')
        // ID категорії елементів образу з міграції V11
        UUID elementsCategoryId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        
        List<Costume> allCostumes = RepositoryProvider.getCostumeRepository().findAll();
        List<Costume> elements = allCostumes.stream()
                .filter(c -> elementsCategoryId.equals(c.getCategoryId()))
                .filter(c -> {
                    if (filter == null || filter.equals("Усі")) return true;
                    String path = c.getImagePath() != null ? c.getImagePath().toLowerCase() : "";
                    switch (filter) {
                        case "Топи": return path.contains("top_");
                        case "Штани": return path.contains("pants_");
                        case "Спідниці": return path.contains("skirt_");
                        case "Капелюхи": return path.contains("hat_") || path.contains("pirate_hat");
                        case "Маски": return path.contains("mask_") || path.contains("zorro_mask");
                        case "Аксесуари": return path.contains("acc_") || path.contains("red_cape");
                        default: return true;
                    }
                })
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

        // 1. Анімація появи
        canvasItem.setScaleX(0);
        canvasItem.setScaleY(0);
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(300), canvasItem);
        st.setToX(1);
        st.setToY(1);
        st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        st.play();

        // 2. Контекстне меню
        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
        javafx.scene.control.MenuItem bringForward = new javafx.scene.control.MenuItem("На передній план");
        bringForward.setOnAction(ev -> canvasItem.toFront());
        javafx.scene.control.MenuItem sendBackward = new javafx.scene.control.MenuItem("На задній план");
        sendBackward.setOnAction(ev -> canvasItem.toBack());
        javafx.scene.control.MenuItem flip = new javafx.scene.control.MenuItem("Віддзеркалити");
        flip.setOnAction(ev -> canvasItem.setScaleX(canvasItem.getScaleX() * -1));
        javafx.scene.control.MenuItem delete = new javafx.scene.control.MenuItem("Видалити");
        delete.setOnAction(ev -> {
            canvasPane.getChildren().remove(canvasItem);
            addedItems.remove(costume);
            updateTotalPrice();
        });
        contextMenu.getItems().addAll(bringForward, sendBackward, flip, new javafx.scene.control.SeparatorMenuItem(), delete);

        // Drag and Drop логіка
        canvasItem.setOnMousePressed((MouseEvent event) -> {
            dragDeltaX = canvasItem.getLayoutX() - event.getSceneX();
            dragDeltaY = canvasItem.getLayoutY() - event.getSceneY();
        });

        canvasItem.setOnMouseDragged((MouseEvent event) -> {
            canvasItem.setLayoutX(event.getSceneX() + dragDeltaX);
            canvasItem.setLayoutY(event.getSceneY() + dragDeltaY);
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
        
        // Виклик контекстного меню правим кліком
        canvasItem.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                contextMenu.show(canvasItem, event.getScreenX(), event.getScreenY());
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
            backgroundImage.setScaleX(1.0);
            backgroundImage.setScaleY(1.0);
            backgroundImage.setTranslateX(0);
            backgroundImage.setTranslateY(0);
            
            // Дозволяємо перетягувати фон
            final double[] bgDragDelta = new double[2];
            backgroundImage.setOnMousePressed((MouseEvent event) -> {
                bgDragDelta[0] = backgroundImage.getTranslateX() - event.getSceneX();
                bgDragDelta[1] = backgroundImage.getTranslateY() - event.getSceneY();
            });
            backgroundImage.setOnMouseDragged((MouseEvent event) -> {
                backgroundImage.setTranslateX(event.getSceneX() + bgDragDelta[0]);
                backgroundImage.setTranslateY(event.getSceneY() + bgDragDelta[1]);
            });
            
            // Дозволяємо масштабувати фон
            backgroundImage.setOnScroll((javafx.scene.input.ScrollEvent event) -> {
                double zoomFactor = 1.05;
                if (event.getDeltaY() < 0) {
                    zoomFactor = 1 / zoomFactor;
                }
                
                double newScaleX = backgroundImage.getScaleX() * zoomFactor;
                double newScaleY = backgroundImage.getScaleY() * zoomFactor;
                if (newScaleX > 0.2 && newScaleX < 5.0) {
                    backgroundImage.setScaleX(newScaleX);
                    backgroundImage.setScaleY(newScaleY);
                }
                event.consume();
            });
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
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PNG Image", "*.png"),
            new FileChooser.ExtensionFilter("PDF Lookbook", "*.pdf")
        );
        fileChooser.setInitialFileName("my_custom_look");
        
        File file = fileChooser.showSaveDialog(canvasContainer.getScene().getWindow());
        if (file != null) {
            try {
                if (file.getName().toLowerCase().endsWith(".pdf")) {
                    exportToPdf(snapshot, file);
                    showAlert("Успіх", "Ваш Лукбук успішно збережено у PDF!");
                } else {
                    ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
                    showAlert("Успіх", "Ваш образ успішно збережено як зображення!");
                }
                
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
            } catch (Exception e) {
                showAlert("Помилка", "Не вдалося зберегти файл: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void exportToPdf(WritableImage snapshot, File file) throws Exception {
        com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(file);
        com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
        com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);

        byte[] fontBytes = getClass().getResourceAsStream("/fonts/Arial.ttf").readAllBytes();
        com.itextpdf.kernel.font.PdfFont font = com.itextpdf.kernel.font.PdfFontFactory.createFont(fontBytes, com.itextpdf.io.font.PdfEncodings.IDENTITY_H);
        document.setFont(font);

        document.add(new com.itextpdf.layout.element.Paragraph("Мій унікальний образ (Carnival Rental)")
                .setFontSize(20).setBold().setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", baos);
        com.itextpdf.layout.element.Image pdfImage = new com.itextpdf.layout.element.Image(
                com.itextpdf.io.image.ImageDataFactory.create(baos.toByteArray()));
        pdfImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
        pdfImage.setAutoScale(true);
        document.add(pdfImage);

        document.add(new com.itextpdf.layout.element.Paragraph("\nСписок використаних елементів:").setBold().setFontSize(16));

        com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(
                com.itextpdf.layout.properties.UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();
        table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Назва елементу").setBold()));
        table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Ціна оренди (₴)").setBold()));

        for (Costume c : addedItems) {
            table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(c.getName())));
            table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.format("%.2f", c.getPricePerDay()))));
        }
        document.add(table);

        document.add(new com.itextpdf.layout.element.Paragraph("\nЗагальна вартість оренди: " + String.format("%.2f ₴", totalPrice))
                .setBold().setFontSize(14).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));

        document.close();
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
