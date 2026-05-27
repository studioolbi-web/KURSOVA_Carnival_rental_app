package com.oliinyk.costumes.service.export;

import java.io.File;

/**
 * Патерн Strategy: Інтерфейс для різних стратегій експорту даних.
 *
 * @param <T> тип даних, які будуть експортуватися
 */
public interface ExportStrategy<T> {
    /**
     * Виконує експорт даних у вказаний файл.
     *
     * @param data дані для експорту
     * @param targetFile цільовий файл
     * @throws Exception якщо під час експорту сталася помилка
     */
    void export(T data, File targetFile) throws Exception;
}
