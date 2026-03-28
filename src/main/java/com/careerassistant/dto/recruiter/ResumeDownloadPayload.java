package com.careerassistant.dto.recruiter;

public record ResumeDownloadPayload(
        String fileName,
        byte[] content
) {
}
