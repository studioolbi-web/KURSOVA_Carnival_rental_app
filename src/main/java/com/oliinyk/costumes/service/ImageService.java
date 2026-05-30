package com.oliinyk.costumes.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/** Сервіс для управління зображеннями костюмів. */
public class ImageService {

    private static final String IMAGE_DIR = System.getProperty("user.home") + "/.carnival-rental/images";

    /**
     * Конструктор сервісу. Створює директорію для збереження зображень, якщо вона не існує.
     *
     * @throws RuntimeException якщо не вдалося створити директорію
     */
    public ImageService() {
        try {
            Files.createDirectories(Paths.get(IMAGE_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Не вдалося створити директорію для зображень", e);
        }
    }

    /**
     * Копіює обраний файл зображення у внутрішню директорію додатку з унікальним ім'ям.
     *
     * @param sourceFile Вихідний файл зображення
     * @return Відносний шлях до збереженого зображення у системі або null, якщо файл не вказано
     * @throws RuntimeException при помилках копіювання файлу
     */
    public String saveImage(File sourceFile) {
        if (sourceFile == null) return null;

        String extension = "";
        String fileName = sourceFile.getName();
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i);
        }

        String newFileName = UUID.randomUUID().toString() + extension;
        Path targetPath = Paths.get(IMAGE_DIR, newFileName);

        try {
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            // Повертаємо відносний шлях для збереження в БД
            return IMAGE_DIR + "/" + newFileName;
        } catch (IOException e) {
            throw new RuntimeException("Помилка при збереженні файлу зображення", e);
        }
    }
}
