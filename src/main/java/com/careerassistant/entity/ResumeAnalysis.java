package com.careerassistant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ResumeAnalysis extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false, unique = true)
    private Resume resume;

    @Column(nullable = false)
    private Integer atsScore;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String extractedSkills;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String missingSkills;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String suggestions;
}
