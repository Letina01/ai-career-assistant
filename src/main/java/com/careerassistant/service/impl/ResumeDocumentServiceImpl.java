package com.careerassistant.service.impl;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import java.io.ByteArrayOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import com.careerassistant.service.ResumeDocumentService;

@Service
@Slf4j
public class ResumeDocumentServiceImpl implements ResumeDocumentService {

    @Override
    public byte[] generatePdf(String resumeText, String candidateName) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Add header with candidate name
            Paragraph header = new Paragraph(candidateName)
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(header);
            
            // Add separator line
            Paragraph separator = new Paragraph("_".repeat(80))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8);
            document.add(separator);
            
            // Add resume content
            for (String line : resumeText.split("\n")) {
                if (line.trim().isEmpty()) {
                    document.add(new Paragraph(""));
                } else if (line.toUpperCase().equals(line) && line.trim().length() > 0) {
                    // Section headers (all caps)
                    Paragraph sectionHeader = new Paragraph(line)
                            .setFontSize(12)
                            .setBold()
                            .setMarginTop(8)
                            .setMarginBottom(4);
                    document.add(sectionHeader);
                } else {
                    // Regular content
                    Paragraph para = new Paragraph(line)
                            .setFontSize(10)
                            .setMarginBottom(2);
                    document.add(para);
                }
            }
            
            document.close();
            log.info("PDF generated successfully for candidate: {}", candidateName);
            return outputStream.toByteArray();
            
        } catch (Exception ex) {
            log.error("Failed to generate PDF for candidate: {}", candidateName, ex);
            throw new RuntimeException("Failed to generate PDF", ex);
        }
    }

    @Override
    public byte[] generateDocx(String resumeText, String candidateName) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            XWPFDocument document = new XWPFDocument();
            
            // Add header with candidate name
            XWPFParagraph headerPara = document.createParagraph();
            headerPara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun headerRun = headerPara.createRun();
            headerRun.setText(candidateName);
            headerRun.setBold(true);
            headerRun.setFontSize(16);
            
            // Add separator
            XWPFParagraph sepPara = document.createParagraph();
            XWPFRun sepRun = sepPara.createRun();
            sepRun.setText("_".repeat(80));
            sepRun.setFontSize(8);
            
            // Add resume content
            for (String line : resumeText.split("\n")) {
                XWPFParagraph para = document.createParagraph();
                XWPFRun run = para.createRun();
                
                if (line.trim().isEmpty()) {
                    // Empty line
                    run.setText("");
                } else if (line.toUpperCase().equals(line) && line.trim().length() > 0) {
                    // Section headers (all caps)
                    run.setText(line);
                    run.setBold(true);
                    run.setFontSize(12);
                    para.setSpacingBefore(100);
                    para.setSpacingAfter(50);
                } else {
                    // Regular content
                    run.setText(line);
                    run.setFontSize(10);
                    para.setSpacingAfter(20);
                }
            }
            
            document.write(outputStream);
            log.info("DOCX generated successfully for candidate: {}", candidateName);
            
            byte[] result = outputStream.toByteArray();
            document.close();
            outputStream.close();
            return result;
            
        } catch (Exception ex) {
            log.error("Failed to generate DOCX for candidate: {}", candidateName, ex);
            throw new RuntimeException("Failed to generate DOCX", ex);
        }
    }
}
