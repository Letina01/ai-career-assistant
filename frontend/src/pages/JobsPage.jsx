import { useEffect, useState } from 'react';
import { applyToExternalJob, applyToJob, fetchApplicantJobs, fetchJobRecommendations, fetchMyExternalApplications, fetchProfile, fetchResumes, saveJob } from '../api';
import { Briefcase, MapPin, TrendingUp, BookmarkPlus, Search, Send, Clock, Filter, ChevronRight, Check, ExternalLink } from 'lucide-react';

export default function JobsPage() {
  const [resumes, setResumes] = useState([]);
  const [filters, setFilters] = useState({ resumeId: '', query: 'Software Engineer', location: 'India' });
  const [jobs, setJobs] = useState([]);
  const [postedJobs, setPostedJobs] = useState([]);
  const [myApplications, setMyApplications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [savedJobs, setSavedJobs] = useState(new Set());
  const [appliedJobs, setAppliedJobs] = useState(new Set());
  const [activeTab, setActiveTab] = useState('search');

  useEffect(() => {
    Promise.all([
      fetchResumes(),
      fetchApplicantJobs(),
      fetchProfile(),
      fetchMyExternalApplications()
    ]).then(([resumeData, postedJobsData, profile, applicationsData]) => {
      setResumes(resumeData);
      setPostedJobs(postedJobsData);
      setMyApplications(applicationsData?.data || []);
      
      if (applicationsData?.data) {
        const applied = new Set(applicationsData.data.map(app => `${app.jobTitle}-${app.company}`));
        setAppliedJobs(applied);
      }
      
      if (resumeData[0]) {
        setFilters(prev => ({
          ...prev,
          resumeId: resumeData[0].resumeId,
          query: profile?.preferredRole || prev.query,
          location: profile?.preferredLocation || prev.location
        }));
      }
    }).catch(() => {});
  }, []);

  const search = async (event) => {
    event?.preventDefault();
    if (!filters.resumeId) {
      alert('Please select a resume first');
      return;
    }
    setLoading(true);
    try {
      const data = await fetchJobRecommendations(filters.resumeId, filters.query, filters.location);
      setJobs(data);
      setActiveTab('results');
    } catch (err) {
      alert('Failed to fetch jobs');
    } finally {
      setLoading(false);
    }
  };

  const persistJob = async (job) => {
    try {
      await saveJob({ title: job.title, company: job.company, applyLink: job.applyLink, matchScore: job.matchScore });
      setSavedJobs(prev => new Set([...prev, job.title]));
      setTimeout(() => {
        setSavedJobs(prev => {
          const next = new Set(prev);
          next.delete(job.title);
          return next;
        });
      }, 2000);
    } catch (err) {
      alert('Failed to save job');
    }
  };

  const applyToExternalJobFn = async (job) => {
    const jobKey = `${job.title}-${job.company}`;
    if (appliedJobs.has(jobKey)) {
      alert('Already applied!');
      return;
    }
    try {
      const response = await applyToExternalJob({
        jobTitle: job.title, company: job.company, location: job.location || 'Remote',
        applyLink: job.applyLink, matchScore: job.matchScore
      });
      if (response.success) {
        setAppliedJobs(prev => new Set([...prev, jobKey]));
        setMyApplications(prev => [...prev, response.data]);
      }
    } catch (err) {
      alert('Failed to apply');
    }
  };

  const apply = async (jobId) => {
    try {
      await applyToJob(jobId, { resumeId: Number(filters.resumeId) });
      alert('Applied successfully!');
    } catch (err) {
      alert('Failed to apply');
    }
  };

  const getMatchColor = (score) => score >= 80 ? '#10b981' : score >= 60 ? '#f59e0b' : '#6b7280';
  const getMatchBg = (score) => score >= 80 ? 'rgba(16, 185, 129, 0.1)' : score >= 60 ? 'rgba(245, 158, 11, 0.1)' : 'rgba(107, 114, 128, 0.1)';

  return (
    <div className="jobs-wrapper">
      <header className="jobs-header">
        <div>
          <p className="eyebrow">Job Search</p>
          <h1>Find Your Perfect Role</h1>
        </div>
        <div className="jobs-tabs">
          <button className={`tab ${activeTab === 'search' ? 'active' : ''}`} onClick={() => setActiveTab('search')}>
            <Search size={16} /> Search
          </button>
          <button className={`tab ${activeTab === 'results' ? 'active' : ''}`} onClick={() => setActiveTab('results')}>
            <Briefcase size={16} /> Results ({jobs.length})
          </button>
          <button className={`tab ${activeTab === 'applied' ? 'active' : ''}`} onClick={() => setActiveTab('applied')}>
            <Clock size={16} /> Applied ({myApplications.length})
          </button>
          {postedJobs.length > 0 && (
            <button className={`tab ${activeTab === 'internal' ? 'active' : ''}`} onClick={() => setActiveTab('internal')}>
              <Briefcase size={16} /> Internal ({postedJobs.length})
            </button>
          )}
        </div>
      </header>

      {activeTab === 'search' && (
        <div className="jobs-main">
          <div className="search-card">
            <form className="search-form" onSubmit={search}>
              <div className="search-row">
                <div className="search-field">
                  <label>Select Resume</label>
                  <select value={filters.resumeId} onChange={(e) => setFilters({ ...filters, resumeId: e.target.value })}>
                    <option value="">Choose resume...</option>
                    {resumes.map(r => <option key={r.resumeId} value={r.resumeId}>{r.candidateName}</option>)}
                  </select>
                </div>
                <div className="search-field">
                  <label>Job Title</label>
                  <input value={filters.query} onChange={(e) => setFilters({ ...filters, query: e.target.value })} placeholder="e.g., Software Engineer" />
                </div>
                <div className="search-field">
                  <label>Location</label>
                  <input value={filters.location} onChange={(e) => setFilters({ ...filters, location: e.target.value })} placeholder="e.g., Bangalore, Remote" />
                </div>
              </div>
              <button type="submit" className="btn-search" disabled={loading}>
                {loading ? 'Searching...' : <><Search size={18} /> Find Jobs</>}
              </button>
            </form>
          </div>

          <div className="search-tips">
            <h3>Tips for better results</h3>
            <div className="tips-grid">
              <div className="tip">
                <span className="tip-icon">💡</span>
                <span>Use specific job titles like "Full Stack Developer" instead of generic terms</span>
              </div>
              <div className="tip">
                <span className="tip-icon">📍</span>
                <span>Try "Remote" or specific cities for location-based search</span>
              </div>
              <div className="tip">
                <span className="tip-icon">📄</span>
                <span>Upload a detailed resume for better skill matching</span>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'results' && (
        <div className="jobs-results">
          {jobs.length > 0 ? (
            <div className="jobs-grid-new">
              {jobs.map((job, index) => (
                <div key={index} className="job-card-new">
                  <div className="job-card-top">
                    <div className="job-company-icon">
                      <Briefcase size={20} />
                    </div>
                    <div className="job-info">
                      <h3>{job.title}</h3>
                      <p>{job.company}</p>
                    </div>
                    <div className="match-indicator" style={{ background: getMatchBg(job.matchScore) }}>
                      <TrendingUp size={14} />
                      <span style={{ color: getMatchColor(job.matchScore) }}>{job.matchScore}%</span>
                    </div>
                  </div>
                  
                  <div className="job-actions">
                    <button
                      className={`btn-apply ${appliedJobs.has(`${job.title}-${job.company}`) ? 'applied' : ''}`}
                      onClick={() => applyToExternalJobFn(job)}
                      disabled={appliedJobs.has(`${job.title}-${job.company}`)}
                    >
                      {appliedJobs.has(`${job.title}-${job.company}`) ? <><Check size={16} /> Applied</> : <><Send size={16} /> Apply</>}
                    </button>
                    <a href={job.applyLink} target="_blank" rel="noreferrer" className="btn-visit">
                      <ExternalLink size={16} /> Visit
                    </a>
                    <button
                      className={`btn-save ${savedJobs.has(job.title) ? 'saved' : ''}`}
                      onClick={() => persistJob(job)}
                    >
                      <BookmarkPlus size={16} />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-results">
              <Search size={48} />
              <h3>No jobs found</h3>
              <p>Try adjusting your search criteria or upload a resume first</p>
              <button onClick={() => setActiveTab('search')}>Start New Search</button>
            </div>
          )}
        </div>
      )}

      {activeTab === 'applied' && (
        <div className="applied-section">
          {myApplications.length > 0 ? (
            <div className="applied-list">
              {myApplications.map((app, i) => (
                <div key={i} className="applied-card">
                  <div className="applied-info">
                    <h4>{app.jobTitle}</h4>
                    <p>{app.company} • {app.location}</p>
                  </div>
                  <div className="applied-meta">
                    <span className="applied-match" style={{ color: getMatchColor(app.matchScore) }}>
                      {app.matchScore}% match
                    </span>
                    <span className="applied-date">
                      <Clock size={12} /> {new Date(app.appliedDate).toLocaleDateString()}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-results">
              <Clock size={48} />
              <h3>No applications yet</h3>
              <p>Apply to jobs to track your applications here</p>
            </div>
          )}
        </div>
      )}

      {activeTab === 'internal' && postedJobs.length > 0 && (
        <div className="jobs-results">
          <div className="jobs-grid-new">
            {postedJobs.map((job) => (
              <div key={job.jobId} className="job-card-new internal">
                <div className="job-card-top">
                  <div className="job-company-icon internal">
                    <Briefcase size={20} />
                  </div>
                  <div className="job-info">
                    <h3>{job.title}</h3>
                    <p>{job.company} • {job.location}</p>
                    <span className="job-skills-tag">{job.requiredSkills?.split(',')[0]}</span>
                  </div>
                  <span className="internal-badge">Internal</span>
                </div>
                <div className="job-actions">
                  <button className="btn-apply" onClick={() => apply(job.jobId)}>
                    <Send size={16} /> Apply
                  </button>
                  {job.applyLink && (
                    <a href={job.applyLink} target="_blank" rel="noreferrer" className="btn-visit">
                      <ExternalLink size={16} /> More
                    </a>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
