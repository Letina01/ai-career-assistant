package com.careerassistant.controller;

import com.careerassistant.dto.skillgap.SkillGapRequest;
import com.careerassistant.dto.skillgap.SkillGapResponse;
import com.careerassistant.service.SkillGapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skill-gap")
@RequiredArgsConstructor
@PreAuthorize("hasRole('APPLICANT')")
public class SkillGapController {

    private final SkillGapService skillGapService;

    @PostMapping
    public SkillGapResponse analyze(@Valid @RequestBody SkillGapRequest request) {
        return skillGapService.analyze(request);
    }
}
