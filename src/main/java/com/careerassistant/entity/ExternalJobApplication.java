package com.careerassistant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "external_job_applications",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"applicant_id", "job_title", "company"}, name = "uk_applicant_job_company")
    }
)
public class ExternalJobApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private UserAccount applicant;

    @Column(nullable = false, length = 255)
    private String jobTitle;

    @Column(nullable = false, length = 255)
    private String company;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(nullable = false, length = 2048)
    private String applyLink;

    @Column(nullable = false)
    private Integer matchScore;

    @Column(nullable = false)
    private String applicationStatus;  // APPLIED, IN_REVIEW, REJECTED, ACCEPTED

    @Column(length = 500)
    private String notes;

    // Constructor
    public ExternalJobApplication() {}

    public ExternalJobApplication(UserAccount applicant, String jobTitle, String company, 
                                  String location, String applyLink, Integer matchScore) {
        this.applicant = applicant;
        this.jobTitle = jobTitle;
        this.company = company;
        this.location = location;
        this.applyLink = applyLink;
        this.matchScore = matchScore;
        this.applicationStatus = "APPLIED";
    }
}
