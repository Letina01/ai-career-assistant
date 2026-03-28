package com.careerassistant.service;

import com.careerassistant.dto.resume.ResumeSummaryResponse;
import com.careerassistant.dto.resume.ResumeUploadResponse;
import com.careerassistant.dto.resume.ResumeAnalysisReportResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService {
    ResumeUploadResponse uploadAndAnalyze(String candidateName, String email, MultipartFile file);
    List<ResumeSummaryResponse> getAllResumes();
    ResumeAnalysisReportResponse getAnalysisReport(Long resumeId);
}
