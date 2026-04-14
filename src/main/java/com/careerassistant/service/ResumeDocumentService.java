package com.careerassistant.service;

public interface ResumeDocumentService {
    /**
     * Generate PDF from resume text
     * @param resumeText The resume content
     * @param candidateName Name of the candidate
     * @return Byte array containing PDF content
     */
    byte[] generatePdf(String resumeText, String candidateName);

    /**
     * Generate DOCX from resume text
     * @param resumeText The resume content
     * @param candidateName Name of the candidate
     * @return Byte array containing DOCX content
     */
    byte[] generateDocx(String resumeText, String candidateName);
}
