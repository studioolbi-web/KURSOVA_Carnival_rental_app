package com.oliinyk.costumes.view;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.oliinyk.costumes.viewmodel.SettingsViewModel;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

/** Контролер налаштувань. Керує темою та режимом відображення каталогу. */
public class SettingsController {

    @FXML private ToggleButton themeToggle;
    @FXML private ToggleButton gridToggle;
    @FXML private ToggleButton listToggle;
    @FXML private javafx.scene.control.ComboBox<String> languageCombo;
    
    @FXML private javafx.scene.control.Label titleLabel;
    @FXML private javafx.scene.control.Label appearanceLabel;
    @FXML private javafx.scene.control.Label themeLabel;
    @FXML private javafx.scene.control.Label catalogModeLabel;
    @FXML private javafx.scene.control.Label languageLabel;

    private SettingsViewModel viewModel;

    public void setViewModel(SettingsViewModel viewModel) {
        this.viewModel = viewModel;
        setupUI();
    }

    private void setupUI() {
        titleLabel.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("settings.title"));
        appearanceLabel.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("settings.appearance"));
        themeLabel.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("settings.theme"));
        catalogModeLabel.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("settings.catalog.mode"));
        gridToggle.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("settings.catalog.grid"));
        listToggle.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("settings.catalog.list"));
        languageLabel.textProperty().bind(com.oliinyk.costumes.util.I18nManager.createStringBinding("settings.language"));

        themeToggle.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(() -> {
            return themeToggle.isSelected() ? com.oliinyk.costumes.util.I18nManager.get("settings.theme.off") : com.oliinyk.costumes.util.I18nManager.get("settings.theme.on");
        }, themeToggle.selectedProperty(), com.oliinyk.costumes.util.I18nManager.localeProperty()));

        // Налаштування теми
        themeToggle.setSelected(viewModel.isDarkTheme());

        themeToggle.setOnAction(
                e -> {
                    viewModel.setTheme(themeToggle.isSelected());
                    if (themeToggle.isSelected()) {
                        Application.setUserAgentStylesheet(
                                new PrimerDark().getUserAgentStylesheet());
                    } else {
                        Application.setUserAgentStylesheet(
                                new PrimerLight().getUserAgentStylesheet());
                    }
                });

        // Налаштування режиму каталогу
        ToggleGroup viewGroup = new ToggleGroup();
        gridToggle.setToggleGroup(viewGroup);
        listToggle.setToggleGroup(viewGroup);

        if (viewModel.isGridView()) {
            gridToggle.setSelected(true);
        } else {
            listToggle.setSelected(true);
        }

        // Налаштування мови
        languageCombo.getItems().addAll("Українська", "English");
        if (com.oliinyk.costumes.util.I18nManager.getLocale().getLanguage().equals("en")) {
            languageCombo.setValue("English");
        } else {
            languageCombo.setValue("Українська");
        }
        
        languageCombo.setOnAction(e -> {
            if ("English".equals(languageCombo.getValue())) {
                com.oliinyk.costumes.util.I18nManager.setLocale(java.util.Locale.of("en", "US"));
            } else {
                com.oliinyk.costumes.util.I18nManager.setLocale(java.util.Locale.of("uk", "UA"));
            }
        });
    }



    @FXML
    private void onGridViewSelected() {
        viewModel.setViewMode(true);
    }

    @FXML
    private void onListViewSelected() {
        viewModel.setViewMode(false);
    }
}
