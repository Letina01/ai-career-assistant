# AI Career Assistant

Monolithic AI-based Career Assistant web application built with Spring Boot, React, MySQL, Apache Tika, OpenAI/Gemini integration, JWT authentication, RBAC, Docker, and GitHub Actions.

## Included Modules

- Resume upload and PDF parsing with Apache Tika
- AI resume analysis with extracted skills, ATS score, missing skills, and suggestions
- Job recommendations from RapidAPI-style providers with skill-based ranking
- Context-aware chatbot with persisted chat history
- Interview preparation with generated questions, answers, and roadmap
- Skill gap analyzer with learning roadmap
- Resume improvement generator for rewriting sections
- Dashboard for resume history, ATS scores, and saved jobs
- JWT authentication with `APPLICANT` and `RECRUITER` roles
- Recruiter job posting, applicant review, ATS visibility, and shortlist/reject actions

## Folder Structure

```text
.
|-- src/main/java/com/careerassistant
|   |-- config
|   |-- controller
|   |-- dto
|   |-- entity
|   |-- exception
|   |-- repository
|   |-- security
|   `-- service
|-- src/main/resources/application.yml
|-- frontend
|   |-- src/components
|   |-- src/context
|   |-- src/pages
|   |-- src/api.js
|   `-- src/styles.css
|-- .github/workflows/ci-cd.yml
|-- docker-compose.yml
|-- schema.sql
`-- README.md
```

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register applicant or recruiter |
| POST | `/api/auth/login` | Authenticate and receive JWT |
| POST | `/api/auth/request-password-reset` | Request one-time reset token via email |
| POST | `/api/auth/reset-password` | Reset password using token + new password |
| POST | `/api/resumes/upload` | Applicant uploads PDF and gets AI analysis |
| GET | `/api/resumes` | Applicant lists owned resumes |
| GET | `/api/jobs/recommend` | Applicant fetches external job recommendations |
| POST | `/api/jobs/save` | Applicant saves a recommended job |
| GET | `/api/jobs/saved` | Applicant lists saved jobs |
| GET | `/api/applicant/jobs` | Applicant lists recruiter-posted jobs |
| POST | `/api/applicant/jobs/{jobId}/apply` | Applicant applies with a selected resume |
| GET | `/api/applicant/applications` | Applicant views application history |
| POST | `/api/recruiter/jobs` | Recruiter posts a new job |
| GET | `/api/recruiter/jobs` | Recruiter lists owned jobs |
| GET | `/api/recruiter/applications` | Recruiter reviews applicants and ATS scores |
| PUT | `/api/recruiter/applications/{applicationId}` | Recruiter updates status to `SHORTLISTED` or `REJECTED` |
| POST | `/api/chat` | Applicant sends chatbot message with context |
| POST | `/api/interview/prepare` | Applicant generates interview questions, answers, roadmap |
| POST | `/api/skill-gap` | Applicant compares resume skills with target role |
| POST | `/api/resume-improvements` | Applicant rewrites a resume section |
| GET | `/api/dashboard` | Applicant dashboard aggregates |

## Database Schema

- `user_account(id, full_name, email, password, role, created_at, updated_at)`
- `resume(id, owner_id, candidate_name, email, file_name, extracted_text, created_at, updated_at)`
- `resume_analysis(id, resume_id, ats_score, extracted_skills, missing_skills, suggestions, created_at, updated_at)`
- `saved_job(id, owner_id, title, company, apply_link, match_score, created_at, updated_at)`
- `chat_session(id, owner_id, title, created_at, updated_at)`
- `chat_message(id, session_id, role, content, created_at, updated_at)`
- `job_posting(id, recruiter_id, title, company, location, description, required_skills, apply_link, created_at, updated_at)`
- `job_application(id, job_posting_id, applicant_id, resume_id, status, created_at, updated_at)`

## Architecture

- Backend uses Controller -> Service -> Repository layering.
- Spring Security + JWT provide stateless authentication.
- RBAC distinguishes `APPLICANT` and `RECRUITER` capabilities.
- Spring Data JPA handles MySQL persistence.
- `@RestControllerAdvice` centralizes exception handling.
- AI access is abstracted behind `AiService` with OpenAI/Gemini support and fallback heuristics.
- React consumes the monolith through REST APIs and switches UI by authenticated role.

## Role Flows

### Applicant

- Register or login as `APPLICANT`
- Upload resume
- Chat with AI
- Get job suggestions
- Apply for recruiter-posted jobs
- Track application status
- Use interview prep, skill gap, and resume improvement tools

### Recruiter

- Register or login as `RECRUITER`
- Post jobs
- View applicants
- See applicant email, resume reference, and ATS score
- Shortlist or reject candidates

## Run Locally

### Backend

```bash
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

### Docker

```bash
docker compose up --build
```

## Deployment

1. Provision an AWS EC2 instance with Docker and Docker Compose.
2. Set environment variables for MySQL, AI provider keys, job API keys, and JWT secret.
3. Pull the repository on EC2.
4. Run `docker compose up -d --build`.
5. Extend `.github/workflows/ci-cd.yml` with your EC2 SSH deployment secrets.

## Notes

- If AI keys are not configured, deterministic fallback logic keeps resume analysis, chatbot, interview prep, and resume rewriting usable.
- The job integration is wired for JSearch on RapidAPI; replace host and URL if you use another provider.
- Applicant data is scoped by authenticated owner. Recruiters only see applications tied to their own job postings.
