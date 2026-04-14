package com.careerassistant.service.impl;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class PdfGeneratorService {

    public byte[] generateResumePdf(String candidateName, String content) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            PdfFont titleFont = PdfFontFactory.createFont();
            PdfFont normalFont = PdfFontFactory.createFont();

            Paragraph title = new Paragraph("Resume - " + candidateName)
                    .setFont(titleFont)
                    .setFontSize(18)
                    .setBold()
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            Paragraph divider = new Paragraph("━".repeat(60))
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(divider);

            String[] lines = content.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    document.add(new Paragraph(" "));
                } else if (line.matches("^[A-Z\\s]+$") && line.length() < 50) {
                    Paragraph sectionHeader = new Paragraph(line)
                            .setFont(normalFont)
                            .setFontSize(12)
                            .setBold()
                            .setFontColor(ColorConstants.BLUE);
                    document.add(sectionHeader);
                } else {
                    Paragraph p = new Paragraph(line)
                            .setFont(normalFont)
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.JUSTIFIED);
                    document.add(p);
                }
            }

            document.close();
            log.info("Generated PDF for candidate: {}", candidateName);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate PDF for {}: {}", candidateName, e.getMessage());
            throw new RuntimeException("Failed to generate resume PDF", e);
        }
    }
}
