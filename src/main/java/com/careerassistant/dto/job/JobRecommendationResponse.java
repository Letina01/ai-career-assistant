package com.careerassistant.dto.job;

public record JobRecommendationResponse(
        String title,
        String company,
        String applyLink,
        Integer matchScore,
        boolean saved
) {
}
