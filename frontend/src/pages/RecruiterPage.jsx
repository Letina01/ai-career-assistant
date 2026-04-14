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

const getStatusColor = (status) => {
  switch (status) {
    case 'SHORTLISTED': return 'status-shortlisted';
    case 'REJECTED': return 'status-rejected';
    default: return 'status-applied';
  }
};

export default function RecruiterPage() {
  const [jobs, setJobs] = useState([]);
  const [applications, setApplications] = useState([]);
  const [selectedApplicant, setSelectedApplicant] = useState(null);
  const [form, setForm] = useState(defaultForm);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [activeTab, setActiveTab] = useState('applicants');
  const [activeJobTab, setActiveJobTab] = useState('post');

  const load = async () => {
    try {
      const [jobsData, applicationsData] = await Promise.all([
        fetchRecruiterJobs(),
        fetchRecruiterApplications()
      ]);
      setJobs(jobsData);
      setApplications(applicationsData);
      setSelectedApplicant((current) => current
        ? applicationsData.find((item) => item.applicationId === current.applicationId) || applicationsData[0] || null
        : applicationsData[0] || null);
    } catch (error) {
      console.error('Failed to load data:', error);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setSuccess(false);
    try {
      await createRecruiterJob(form);
      setForm(defaultForm);
      setSuccess(true);
      setActiveJobTab('myjobs');
      setTimeout(() => setSuccess(false), 3000);
      load();
    } catch (error) {
      console.error('Failed to create job:', error);
    } finally {
      setLoading(false);
    }
  };

  const setStatus = async (applicationId, status) => {
    try {
      await updateApplicationStatus(applicationId, { status });
      load();
    } catch (error) {
      console.error('Failed to update status:', error);
    }
  };

  const downloadResume = async (applicationId) => {
    try {
      const { blob, fileName } = await downloadApplicantResume(applicationId);
      
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName;
      link.style.display = 'none';
      document.body.appendChild(link);
      link.click();
      
      setTimeout(() => {
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      }, 100);
    } catch (error) {
      console.error('Failed to download resume:', error);
      alert('Failed to download resume. Please try again.');
    }
  };

  const shortlistCount = applications.filter(a => a.status === 'SHORTLISTED').length;
  const pendingCount = applications.filter(a => a.status === 'APPLIED').length;

  return (
    <div className="recruiter-layout">
      <div className="recruiter-header">
        <div className="recruiter-title">
          <p className="eyebrow accent-eyebrow">Recruiter Dashboard</p>
          <h2>Manage Hiring Pipeline</h2>
        </div>
        <div className="recruiter-stats">
          <div className="stat-box">
            <span className="stat-num">{jobs.length}</span>
            <span className="stat-lbl">Jobs Posted</span>
          </div>
          <div className="stat-box highlight">
            <span className="stat-num">{applications.length}</span>
            <span className="stat-lbl">Total Applicants</span>
          </div>
          <div className="stat-box">
            <span className="stat-num">{pendingCount}</span>
            <span className="stat-lbl">Pending</span>
          </div>
          <div className="stat-box success">
            <span className="stat-num">{shortlistCount}</span>
            <span className="stat-lbl">Shortlisted</span>
          </div>
        </div>
      </div>

      <div className="recruiter-main">
        <div className="recruiter-left">
          <div className="tab-bar">
            <button 
              className={`tab-btn ${activeJobTab === 'post' ? 'active' : ''}`}
              onClick={() => setActiveJobTab('post')}
            >
              Post Job
            </button>
            <button 
              className={`tab-btn ${activeJobTab === 'myjobs' ? 'active' : ''}`}
              onClick={() => setActiveJobTab('myjobs')}
            >
              My Jobs ({jobs.length})
            </button>
          </div>

          {activeJobTab === 'post' && (
            <SectionCard title="Post New Job" subtitle="Fill details to create listing">
              <form className="compact-form" onSubmit={submit}>
                <div className="form-grid">
                  <input 
                    value={form.title} 
                    onChange={(e) => setForm({ ...form, title: e.target.value })} 
                    placeholder="Job title *" 
                    required 
                  />
                  <input 
                    value={form.company} 
                    onChange={(e) => setForm({ ...form, company: e.target.value })} 
                    placeholder="Company *" 
                    required 
                  />
                  <input 
                    value={form.location} 
                    onChange={(e) => setForm({ ...form, location: e.target.value })} 
                    placeholder="Location *" 
                    required 
                  />
                  <input 
                    value={form.requiredSkills} 
                    onChange={(e) => setForm({ ...form, requiredSkills: e.target.value })} 
                    placeholder="Skills (Java, AWS...) *" 
                    required 
                  />
                </div>
                <textarea 
                  rows={3}
                  value={form.description} 
                  onChange={(e) => setForm({ ...form, description: e.target.value })} 
                  placeholder="Job description & requirements *"
                  required 
                />
                <input 
                  value={form.applyLink} 
                  onChange={(e) => setForm({ ...form, applyLink: e.target.value })} 
                  placeholder="External application link (optional)" 
                />
                <button type="submit" disabled={loading} className="btn-primary">
                  {loading ? 'Publishing...' : 'Publish Job'}
                </button>
                {success && <p className="success-text">Job published successfully!</p>}
              </form>
            </SectionCard>
          )}

          {activeJobTab === 'myjobs' && (
            <SectionCard title="My Posted Jobs" subtitle={`${jobs.length} job(s) created`}>
              <div className="compact-list">
                {jobs.map((job) => (
                  <div key={job.jobId} className="compact-item">
                    <div className="item-main">
                      <strong>{job.title}</strong>
                      <span className="item-meta">{job.company} • {job.location}</span>
                    </div>
                    <span className="item-skills">{job.requiredSkills?.split(',')[0]}</span>
                  </div>
                ))}
                {jobs.length === 0 && (
                  <div className="empty-state compact-empty">
                    <p>No jobs posted yet</p>
                    <button className="btn-ghost-sm" onClick={() => setActiveJobTab('post')}>
                      Post Your First Job
                    </button>
                  </div>
                )}
              </div>
            </SectionCard>
          )}

          <div className="applicant-list-section">
            <div className="section-header">
              <h3>Applicants</h3>
              <span className="badge">{applications.length}</span>
            </div>
            <div className="applicant-scroll">
              {applications.map((item) => (
                <button
                  key={item.applicationId}
                  type="button"
                  className={`compact-applicant ${selectedApplicant?.applicationId === item.applicationId ? 'selected' : ''}`}
                  onClick={() => setSelectedApplicant(item)}
                >
                  <div className="applicant-avatar">
                    {item.applicantName?.charAt(0).toUpperCase()}
                  </div>
                  <div className="applicant-info">
                    <strong>{item.applicantName}</strong>
                    <span>{item.jobTitle}</span>
                  </div>
                  <div className="applicant-right">
                    <span className="ats-badge">ATS {item.atsScore ?? '?'}</span>
                    <span className={`status-dot ${item.status.toLowerCase()}`}></span>
                  </div>
                </button>
              ))}
              {applications.length === 0 && (
                <div className="empty-state compact-empty">
                  <p>No applicants yet</p>
                  <span className="muted">Candidates will appear here when they apply</span>
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="recruiter-right">
          <SectionCard title="Candidate Details" subtitle="Review and take action">
            {selectedApplicant ? (
              <div className="candidate-detail">
                <div className="candidate-header">
                  <div className="candidate-avatar-lg">
                    {selectedApplicant.applicantName?.charAt(0).toUpperCase()}
                  </div>
                  <div className="candidate-name">
                    <h3>{selectedApplicant.applicantName}</h3>
                    <p className="muted">{selectedApplicant.applicantEmail}</p>
                    <div className="candidate-badges">
                      <span className="ats-badge-lg">ATS: {selectedApplicant.atsScore ?? 'N/A'}%</span>
                      <span className={`status-badge ${getStatusColor(selectedApplicant.status)}`}>
                        {selectedApplicant.status}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="detail-grid">
                  <div className="detail-card">
                    <span className="detail-label">Phone</span>
                    <span className="detail-value">{selectedApplicant.phone || 'Not provided'}</span>
                  </div>
                  <div className="detail-card">
                    <span className="detail-label">City</span>
                    <span className="detail-value">{selectedApplicant.city || 'Not provided'}</span>
                  </div>
                  <div className="detail-card">
                    <span className="detail-label">Current Role</span>
                    <span className="detail-value">{selectedApplicant.currentRole || 'Not provided'}</span>
                  </div>
                  <div className="detail-card">
                    <span className="detail-label">Company</span>
                    <span className="detail-value">{selectedApplicant.currentCompany || 'Not provided'}</span>
                  </div>
                  <div className="detail-card">
                    <span className="detail-label">Experience</span>
                    <span className="detail-value">{selectedApplicant.experienceYears ?? 0} years</span>
                  </div>
                  <div className="detail-card">
                    <span className="detail-label">Applied For</span>
                    <span className="detail-value">{selectedApplicant.jobTitle}</span>
                  </div>
                </div>

                <div className="detail-section-sm">
                  <span className="detail-label">Skills</span>
                  <p className="skills-text">{selectedApplicant.skills || 'Not provided'}</p>
                </div>

                <div className="detail-section-sm">
                  <span className="detail-label">Education</span>
                  <p className="skills-text">{selectedApplicant.education || 'Not provided'}</p>
                </div>

                <div className="detail-section-sm">
                  <span className="detail-label">Bio</span>
                  <p className="bio-text">{selectedApplicant.bio || 'Not provided'}</p>
                </div>

                <div className="link-row">
                  {selectedApplicant.linkedinUrl && (
                    <a href={selectedApplicant.linkedinUrl} target="_blank" rel="noreferrer" className="link-btn">
                      LinkedIn
                    </a>
                  )}
                  {selectedApplicant.githubUrl && (
                    <a href={selectedApplicant.githubUrl} target="_blank" rel="noreferrer" className="link-btn">
                      GitHub
                    </a>
                  )}
                  {selectedApplicant.portfolioUrl && (
                    <a href={selectedApplicant.portfolioUrl} target="_blank" rel="noreferrer" className="link-btn">
                      Portfolio
                    </a>
                  )}
                </div>

                <div className="action-buttons">
                  <button 
                    type="button" 
                    className="btn-action btn-shortlist"
                    onClick={() => setStatus(selectedApplicant.applicationId, 'SHORTLISTED')}
                    disabled={selectedApplicant.status === 'SHORTLISTED'}
                  >
                    <span className="btn-icon">&#10003;</span>
                    Shortlist
                  </button>
                  <button 
                    type="button" 
                    className="btn-action btn-reject"
                    onClick={() => setStatus(selectedApplicant.applicationId, 'REJECTED')}
                    disabled={selectedApplicant.status === 'REJECTED'}
                  >
                    <span className="btn-icon">&#10005;</span>
                    Reject
                  </button>
                  <button 
                    type="button" 
                    className="btn-action btn-download"
                    onClick={() => downloadResume(selectedApplicant.applicationId)}
                  >
                    <span className="btn-icon">&#8595;</span>
                    Resume
                  </button>
                </div>
              </div>
            ) : (
              <div className="empty-state">
                <div className="empty-icon">&#128100;</div>
                <p>No candidate selected</p>
                <p className="muted">Click an applicant to view details</p>
              </div>
            )}
          </SectionCard>
        </div>
      </div>
    </div>
  );
}
