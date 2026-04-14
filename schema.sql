CREATE TABLE user_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    city VARCHAR(255),
    preferred_role VARCHAR(255),
    preferred_location VARCHAR(255),
    current_role VARCHAR(255),
    current_company VARCHAR(255),
    recruiter_role VARCHAR(255),
    recruiter_company VARCHAR(255),
    company_website VARCHAR(255),
    experience_years INT,
    notice_period_days INT,
    expected_salary VARCHAR(255),
    bio VARCHAR(1024),
    skills VARCHAR(1000),
    education VARCHAR(1000),
    linkedin_url VARCHAR(255),
    github_url VARCHAR(255),
    portfolio_url VARCHAR(255),
    role VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE INDEX idx_user_email ON user_account(email);
CREATE INDEX idx_user_role ON user_account(role);

CREATE TABLE resume (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    candidate_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    extracted_text LONGTEXT NOT NULL,
    file_data LONGBLOB,
    file_type VARCHAR(100),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_resume_owner FOREIGN KEY (owner_id) REFERENCES user_account(id)
);

CREATE INDEX idx_resume_owner ON resume(owner_id);

CREATE TABLE resume_analysis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resume_id BIGINT NOT NULL UNIQUE,
    ats_score INT NOT NULL,
    extracted_skills LONGTEXT NOT NULL,
    missing_skills LONGTEXT NOT NULL,
    suggestions LONGTEXT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_resume_analysis_resume FOREIGN KEY (resume_id) REFERENCES resume(id)
);

CREATE TABLE saved_job (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    apply_link VARCHAR(1024) NOT NULL,
    match_score INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_saved_job_owner FOREIGN KEY (owner_id) REFERENCES user_account(id)
);

CREATE INDEX idx_saved_job_owner ON saved_job(owner_id);

CREATE TABLE chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_chat_session_owner FOREIGN KEY (owner_id) REFERENCES user_account(id)
);

CREATE TABLE chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    intent VARCHAR(30),
    content LONGTEXT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_chat_message_session FOREIGN KEY (session_id) REFERENCES chat_session(id)
);

CREATE INDEX idx_chat_message_session ON chat_message(session_id);
CREATE INDEX idx_chat_message_created ON chat_message(created_at);

CREATE TABLE email_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resume_id BIGINT,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    error_message VARCHAR(1024),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_email_log_resume FOREIGN KEY (resume_id) REFERENCES resume(id)
);

CREATE TABLE job_posting (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recruiter_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    apply_link VARCHAR(1024) NOT NULL,
    description TEXT NOT NULL,
    required_skills VARCHAR(1000) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_job_posting_recruiter FOREIGN KEY (recruiter_id) REFERENCES user_account(id)
);

CREATE INDEX idx_job_posting_recruiter ON job_posting(recruiter_id);

CREATE TABLE job_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_posting_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_job_application_job FOREIGN KEY (job_posting_id) REFERENCES job_posting(id),
    CONSTRAINT fk_job_application_applicant FOREIGN KEY (applicant_id) REFERENCES user_account(id),
    CONSTRAINT fk_job_application_resume FOREIGN KEY (resume_id) REFERENCES resume(id)
);

CREATE INDEX idx_job_application_applicant ON job_application(applicant_id);
CREATE INDEX idx_job_application_posting ON job_application(job_posting_id);
CREATE INDEX idx_job_application_status ON job_application(status);

CREATE TABLE application_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_application_id BIGINT NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    changed_by_user_id BIGINT NOT NULL,
    note VARCHAR(512),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_status_history_application FOREIGN KEY (job_application_id) REFERENCES job_application(id),
    CONSTRAINT fk_status_history_changed_by FOREIGN KEY (changed_by_user_id) REFERENCES user_account(id)
);

CREATE TABLE password_reset_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(128) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    used_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES user_account(id)
);

-- ============================================
-- MIGRATION: Add resume file columns (for existing databases)
-- Run this if resume table already exists without these columns
-- ============================================
-- ALTER TABLE resume ADD COLUMN file_data LONGBLOB;
-- ALTER TABLE resume ADD COLUMN file_type VARCHAR(100);
