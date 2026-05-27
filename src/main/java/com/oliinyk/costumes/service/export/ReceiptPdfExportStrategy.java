package com.oliinyk.costumes.service.export;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.image.ImageDataFactory;
import com.oliinyk.costumes.model.Costume;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;



/**
 * Стратегія експорту чеку у PDF з QR-кодом (Патерн Strategy).
 */
public class ReceiptPdfExportStrategy implements ExportStrategy<ReceiptData> {

    @Override
    public void export(ReceiptData data, File targetFile) throws Exception {
        PdfWriter writer = new PdfWriter(targetFile);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Підключення шрифта з підтримкою кирилиці
        byte[] fontBytes = getClass().getResourceAsStream("/fonts/Arial.ttf").readAllBytes();
        com.itextpdf.kernel.font.PdfFont font = com.itextpdf.kernel.font.PdfFontFactory.createFont(fontBytes, com.itextpdf.io.font.PdfEncodings.IDENTITY_H);
        document.setFont(font);

        // Заголовок
        Paragraph title = new Paragraph("Carnival Rental - Електронний чек")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        document.add(new Paragraph("Період: " + data.start.format(fmt) + " - " + data.end.format(fmt) + " (" + data.days + " дн.)")
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("\nДеталі оренди:").setBold());

        // Таблиця
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1})).useAllAvailableWidth();
        table.addHeaderCell(new Cell().add(new Paragraph("Назва").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Ціна/день").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Днів").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Сума").setBold()));

        for (Costume c : data.items) {
            table.addCell(new Cell().add(new Paragraph(c.getName())));
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f", c.getPricePerDay()))));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(data.days))));
            BigDecimal lineTotal = c.getPricePerDay().multiply(BigDecimal.valueOf(data.days));
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f", lineTotal))));
        }
        document.add(table);

        document.add(new Paragraph("\nПідсумок:").setBold());
        document.add(new Paragraph("Вартість оренди: " + String.format("%.2f", data.rentalTotal)));
        document.add(new Paragraph("Застава: " + String.format("%.2f", data.deposit)));
        if (data.discount.compareTo(BigDecimal.ZERO) > 0) {
            document.add(new Paragraph("Знижка: -" + String.format("%.2f", data.discount)).setFontColor(ColorConstants.GREEN));
        }
        document.add(new Paragraph("До сплати: " + String.format("%.2f грн", data.grandTotal)).setBold().setFontSize(14));

        // Генерація QR-коду (Англійською для уникнення проблем з кодуванням на iPhone)
        String qrContent = "Carnival Rental\nTotal to pay: " + data.grandTotal + " UAH";
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        
        java.util.Map<com.google.zxing.EncodeHintType, Object> hints = new java.util.HashMap<>();
        hints.put(com.google.zxing.EncodeHintType.CHARACTER_SET, "UTF-8");
        
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200, hints);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();

        Image qrImage = new Image(ImageDataFactory.create(pngData));
        qrImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
        document.add(qrImage);

        document.close();
    }
}
