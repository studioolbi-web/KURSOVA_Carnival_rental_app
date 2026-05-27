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

    private SettingsViewModel viewModel;

    public void setViewModel(SettingsViewModel viewModel) {
        this.viewModel = viewModel;
        setupUI();
    }

    private void setupUI() {
        // Налаштування теми
        themeToggle.setSelected(viewModel.isDarkTheme());
        updateThemeText();

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
                    updateThemeText();
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
    }

    private void updateThemeText() {
        themeToggle.setText(themeToggle.isSelected() ? "Вимкнути" : "Увімкнути");
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
