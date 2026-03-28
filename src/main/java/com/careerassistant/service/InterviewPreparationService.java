package com.careerassistant.service;

import com.careerassistant.dto.interview.InterviewPreparationRequest;
import com.careerassistant.dto.interview.InterviewPreparationResponse;

public interface InterviewPreparationService {
    InterviewPreparationResponse generate(InterviewPreparationRequest request);
}
