package com.oliinyk.costumes.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.oliinyk.costumes.model.Costume;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Сервіс для резервного копіювання та відновлення даних (JSON).
 */
public class BackupService {

    private final ObjectMapper objectMapper;

    public BackupService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // Для LocalDate
    }

    /**
     * Експортує список костюмів у JSON файл.
     *
     * @param costumes список костюмів
     * @param targetFile файл для збереження
     * @throws IOException при помилці запису
     */
    public void exportCostumes(List<Costume> costumes, File targetFile) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(targetFile, costumes);
    }

    /**
     * Імпортує список костюмів з JSON файлу.
     *
     * @param sourceFile файл JSON
     * @return список костюмів
     * @throws IOException при помилці читання
     */
    public List<Costume> importCostumes(File sourceFile) throws IOException {
        return objectMapper.readValue(sourceFile, new TypeReference<List<Costume>>() {});
    }
}
