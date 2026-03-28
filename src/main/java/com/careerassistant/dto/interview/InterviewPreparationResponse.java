package com.careerassistant.dto.interview;

import java.util.List;

public record InterviewPreparationResponse(
        String role,
        List<QuestionAnswer> questionAnswers,
        List<String> roadmap
) {
    public record QuestionAnswer(
            String question,
            String answer
    ) {
    }
}
