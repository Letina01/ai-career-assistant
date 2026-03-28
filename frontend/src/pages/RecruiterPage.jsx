import { useEffect, useState } from 'react';
import { createRecruiterJob, downloadApplicantResume, fetchRecruiterApplications, fetchRecruiterJobs, updateApplicationStatus } from '../api';
import SectionCard from '../components/SectionCard';

const defaultForm = {
  title: '',
  company: '',
  location: '',
  description: '',
  requiredSkills: '',
  applyLink: ''
};

export default function RecruiterPage() {
  const [jobs, setJobs] = useState([]);
  const [applications, setApplications] = useState([]);
  const [selectedApplicant, setSelectedApplicant] = useState(null);
  const [form, setForm] = useState(defaultForm);

  const load = async () => {
    const [jobsData, applicationsData] = await Promise.all([
      fetchRecruiterJobs(),
      fetchRecruiterApplications()
    ]);
    setJobs(jobsData);
    setApplications(applicationsData);
    setSelectedApplicant((current) => current
      ? applicationsData.find((item) => item.applicationId === current.applicationId) || applicationsData[0] || null
      : applicationsData[0] || null);
  };

  useEffect(() => {
    load().catch(() => {});
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    await createRecruiterJob(form);
    setForm(defaultForm);
    load();
  };

  const setStatus = async (applicationId, status) => {
    await updateApplicationStatus(applicationId, { status });
    load();
  };

  const downloadResume = async (applicationId) => {
    const { blob, fileName } = await downloadApplicantResume(applicationId);
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  };

  return (
    <div className="recruiter-layout">
      <section className="hero-panel recruiter-hero">
        <div>
          <p className="eyebrow accent-eyebrow">Recruiter Workspace</p>
          <h2>Post jobs, review applicant profiles, and shortlist faster.</h2>
          <p className="hero-copy">
            Recruiters can create openings, inspect ATS-backed candidates, and review their profile, skills, links, and resume context from one place.
          </p>
        </div>
        <div className="chip-row">
          <span className="chip">Open jobs: {jobs.length}</span>
          <span className="chip">Applicants: {applications.length}</span>
          <span className="chip">Shortlist-ready view</span>
        </div>
      </section>

      <div className="page-grid recruiter-grid">
        <SectionCard title="Post a Job" subtitle="Create a detailed role for applicants">
          <form className="form" onSubmit={submit}>
            <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} placeholder="Job title" />
            <input value={form.company} onChange={(e) => setForm({ ...form, company: e.target.value })} placeholder="Company" />
            <input value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} placeholder="Location" />
            <textarea rows={5} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} placeholder="Responsibilities, stack, and expectations" />
            <input value={form.requiredSkills} onChange={(e) => setForm({ ...form, requiredSkills: e.target.value })} placeholder="Required skills" />
            <input value={form.applyLink} onChange={(e) => setForm({ ...form, applyLink: e.target.value })} placeholder="Application link" />
            <button type="submit">Publish Job</button>
          </form>
        </SectionCard>

        <SectionCard title="Applicants" subtitle="Select an applicant to inspect their profile">
          <div className="list recruiter-applicant-list">
            {applications.map((item) => (
              <button
                key={item.applicationId}
                type="button"
                className={selectedApplicant?.applicationId === item.applicationId ? 'applicant-row selected-row' : 'applicant-row'}
                onClick={() => setSelectedApplicant(item)}
              >
                <div>
                  <strong>{item.applicantName}</strong>
                  <p>{item.jobTitle} | {item.company}</p>
                  <p>{item.currentRole || 'Profile incomplete'} | ATS {item.atsScore ?? 'N/A'}</p>
                </div>
                <span className="score-pill">{item.status}</span>
              </button>
            ))}
            {!applications.length && <p className="muted">No applicants yet.</p>}
          </div>
        </SectionCard>

        <SectionCard title="Applicant Detail" subtitle="Profile, experience, and hiring actions">
          {selectedApplicant ? (
            <div className="stack">
              <div className="detail-header">
                <div>
                  <h3>{selectedApplicant.applicantName}</h3>
                  <p>{selectedApplicant.applicantEmail}</p>
                  <p>{selectedApplicant.phone || 'Phone not added'} | {selectedApplicant.city || 'City not added'}</p>
                </div>
                <div className="score-stack">
                  <span className="score-pill">ATS {selectedApplicant.atsScore ?? 'N/A'}</span>
                  <span className="score-pill">{selectedApplicant.status}</span>
                </div>
              </div>
              <p><strong>Current role:</strong> {selectedApplicant.currentRole || 'Not added'}</p>
              <p><strong>Current company:</strong> {selectedApplicant.currentCompany || 'Not added'}</p>
              <p><strong>Experience:</strong> {selectedApplicant.experienceYears ?? 'N/A'} years</p>
              <p><strong>Skills:</strong> {selectedApplicant.skills || 'Not added'}</p>
              <p><strong>Education:</strong> {selectedApplicant.education || 'Not added'}</p>
              <p><strong>Summary:</strong> {selectedApplicant.bio || 'Not added'}</p>
              <div className="link-strip">
                {selectedApplicant.linkedinUrl && <a href={selectedApplicant.linkedinUrl} target="_blank" rel="noreferrer">LinkedIn</a>}
                {selectedApplicant.githubUrl && <a href={selectedApplicant.githubUrl} target="_blank" rel="noreferrer">GitHub</a>}
                {selectedApplicant.portfolioUrl && <a href={selectedApplicant.portfolioUrl} target="_blank" rel="noreferrer">Portfolio</a>}
              </div>
              <div className="row-actions">
                <button type="button" onClick={() => setStatus(selectedApplicant.applicationId, 'SHORTLISTED')}>Shortlist</button>
                <button type="button" onClick={() => setStatus(selectedApplicant.applicationId, 'REJECTED')}>Reject</button>
                <button type="button" onClick={() => downloadResume(selectedApplicant.applicationId)}>Download Resume</button>
              </div>
            </div>
          ) : (
            <p className="muted">Select an applicant to review their profile.</p>
          )}
        </SectionCard>

        <SectionCard title="My Posted Jobs" subtitle="Current recruiter-owned openings">
          <div className="list">
            {jobs.map((job) => (
              <div key={job.jobId} className="list-item">
                <div>
                  <strong>{job.title}</strong>
                  <p>{job.company} | {job.location}</p>
                  <p>{job.requiredSkills}</p>
                </div>
              </div>
            ))}
            {!jobs.length && <p className="muted">No jobs posted yet.</p>}
          </div>
        </SectionCard>
      </div>
    </div>
  );
}
