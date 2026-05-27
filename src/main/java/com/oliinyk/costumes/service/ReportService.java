package com.oliinyk.costumes.service;

import com.oliinyk.costumes.dto.RentalDTO;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Сервіс для генерації звітів (Вимога розділу 5.4). */
public class ReportService {

    /**
     * Експортує список оренд у CSV файл.
     *
     * @param rentals Список об'єктів RentalDTO для експорту
     * @param file Файл, у який буде здійснено запис
     * @throws IOException при помилках запису у файл
     */
    public void exportRentalsToCsv(List<RentalDTO> rentals, java.io.File file) throws IOException {
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            // Додавання BOM для коректного відображення UTF-8 в Excel
            writer.write('\ufeff');

            // Заголовок
            writer.write(
                    "ID,Користувач,Дата Початку,Дата Кінця,Статус,Загальна Ціна,Застава,Штраф,Костюми\n");

            for (RentalDTO rental : rentals) {
                writer.write(
                        String.format(
                                "%s,%s,%s,%s,%s,\"%.2f\",\"%.2f\",\"%.2f\",\"%s\"\n",
                                rental.getId(),
                                rental.getUserEmail(),
                                rental.getStartDate(),
                                rental.getEndDate(),
                                rental.getStatus(),
                                rental.getTotalPrice(),
                                rental.getTotalDeposit(),
                                rental.getPenaltyAmount(),
                                String.join("; ", rental.getCostumeNames())));
            }
        }
    }
}
