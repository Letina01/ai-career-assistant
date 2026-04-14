package com.careerassistant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Resume extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    @Column(nullable = false)
    private String candidateName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String fileName;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String extractedText;

    @Lob
    @Column(nullable = true)
    private byte[] fileData;

    @Column(nullable = true, length = 100)
    private String fileType;

    @OneToOne(mappedBy = "resume", fetch = FetchType.LAZY)
    private ResumeAnalysis resumeAnalysis;
}
