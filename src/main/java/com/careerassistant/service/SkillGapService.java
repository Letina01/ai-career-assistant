package com.careerassistant.service;

import com.careerassistant.dto.skillgap.SkillGapRequest;
import com.careerassistant.dto.skillgap.SkillGapResponse;

public interface SkillGapService {
    SkillGapResponse analyze(SkillGapRequest request);
}
