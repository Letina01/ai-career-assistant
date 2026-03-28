package com.careerassistant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class UserAccount extends BaseEntity {

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;
    private String city;
    private String preferredRole;
    private String preferredLocation;
    private String currentRole;
    private String currentCompany;
    private String recruiterRole;
    private String recruiterCompany;
    private String companyWebsite;
    private Integer experienceYears;
    private Integer noticePeriodDays;
    private String expectedSalary;

    @Column(length = 1024)
    private String bio;

    @Column(length = 1000)
    private String skills;

    @Column(length = 1000)
    private String education;

    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
