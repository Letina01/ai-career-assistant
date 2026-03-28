import { useEffect, useState } from 'react';
import { applyToJob, fetchApplicantJobs, fetchJobRecommendations, fetchProfile, fetchResumes, saveJob } from '../api';
import SectionCard from '../components/SectionCard';
import { Briefcase, MapPin, TrendingUp, BookmarkPlus, Send } from 'lucide-react';

export default function JobsPage() {
  const [resumes, setResumes] = useState([]);
  const [filters, setFilters] = useState({ resumeId: '', query: 'Software Engineer', location: 'India' });
  const [jobs, setJobs] = useState([]);
  const [postedJobs, setPostedJobs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [savedJobs, setSavedJobs] = useState(new Set());

  useEffect(() => {
    Promise.all([fetchResumes(), fetchApplicantJobs(), fetchProfile()]).then(([resumeData, postedJobsData, profile]) => {
      setResumes(resumeData);
      setPostedJobs(postedJobsData);
      if (resumeData[0]) {
        setFilters((prev) => ({
          ...prev,
          resumeId: resumeData[0].resumeId,
          query: profile?.preferredRole || prev.query,
          location: profile?.preferredLocation || prev.location
        }));
      }
    }).catch(() => {});
  }, []);

  const search = async (event) => {
    event.preventDefault();
    if (!filters.resumeId) {
      alert('Please select a resume first');
      return;
    }

    setLoading(true);
    try {
      const data = await fetchJobRecommendations(filters.resumeId, filters.query, filters.location);
      setJobs(data);
    } catch (err) {
      alert('Failed to fetch jobs. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const persistJob = async (job) => {
    try {
      await saveJob({
        title: job.title,
        company: job.company,
        applyLink: job.applyLink,
        matchScore: job.matchScore
      });
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

  const apply = async (jobId) => {
    try {
      await applyToJob(jobId, { resumeId: Number(filters.resumeId) });
      alert('Application submitted successfully!');
    } catch (err) {
      alert('Failed to apply');
    }
  };

  return (
    <div className="applicant-home">
      <div className="hero-panel">
        <div>
          <p className="eyebrow accent-eyebrow">Job Opportunities</p>
          <h2>Find Your Perfect Role</h2>
          <p className="hero-copy">
            Search for opportunities, view skill-matched job recommendations, apply directly, or save jobs for later. We'll rank jobs based on your resume.
          </p>
        </div>
        <div className="hero-side-card">
          <strong>Smart Matching</strong>
          <div className="feature-mini">
            <span>🎯 Skill-based ranking</span>
            <span>💼 Real job listings</span>
            <span>📍 Location filter</span>
            <span>⭐ Save jobs</span>
          </div>
        </div>
      </div>

      <div className="page-grid">
        <SectionCard title="Search Jobs" subtitle="Find opportunities by role and location">
          <form className="form" onSubmit={search}>
            <select
              value={filters.resumeId}
              onChange={(e) => setFilters({ ...filters, resumeId: e.target.value })}
            >
              <option value="">Select resume for matching</option>
              {resumes.map((resume) => (
                <option key={resume.resumeId} value={resume.resumeId}>
                  {resume.candidateName}
                </option>
              ))}
            </select>
            <input
              value={filters.query}
              onChange={(e) => setFilters({ ...filters, query: e.target.value })}
              placeholder="e.g., Senior Developer, Product Manager"
            />
            <input
              value={filters.location}
              onChange={(e) => setFilters({ ...filters, location: e.target.value })}
              placeholder="e.g., San Francisco, Remote"
            />
            <button type="submit" disabled={loading}>
              {loading ? 'Searching...' : 'Find Jobs'}
            </button>
          </form>
        </SectionCard>

        {jobs.length > 0 && (
          <SectionCard title="Recommended Jobs" subtitle="Ranked by skill match">
            <div className="jobs-grid">
              {jobs.map((job, index) => (
                <div key={`${job.title}-${index}`} className="job-card">
                  <div className="job-header">
                    <div>
                      <h3>{job.title}</h3>
                      <p className="job-company">{job.company}</p>
                    </div>
                    <div className="match-badge">
                      <TrendingUp size={18} />
                      <span>{job.matchScore}%</span>
                    </div>
                  </div>

                  <div className="job-footer">
                    <a href={job.applyLink} target="_blank" rel="noreferrer" className="job-link">
                      <Send size={16} />
                      Apply Now
                    </a>
                    <button
                      type="button"
                      className={`ghost-button ${savedJobs.has(job.title) ? 'saved' : ''}`}
                      onClick={() => persistJob(job)}
                    >
                      <BookmarkPlus size={16} />
                      {savedJobs.has(job.title) ? 'Saved' : 'Save'}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </SectionCard>
        )}

        {jobs.length === 0 && !loading && (
          <SectionCard title="Search Results" subtitle="Run a search to see recommendations">
            <p className="muted">💡 Select a resume and search to see job recommendations.</p>
          </SectionCard>
        )}
      </div>

      {postedJobs.length > 0 && (
        <SectionCard title="Posted Opportunities" subtitle="Jobs posted by recruiters in our platform">
          <div className="jobs-grid">
            {postedJobs.map((job) => (
              <div key={job.jobId} className="job-card internal">
                <div className="job-header">
                  <div>
                    <h3>{job.title}</h3>
                    <p className="job-company">{job.company}</p>
                  </div>
                  <div className="internal-badge">Internal</div>
                </div>

                <div className="job-meta">
                  <span className="job-location">
                    <MapPin size={14} />
                    {job.location}
                  </span>
                </div>

                <p className="job-skills">{job.requiredSkills}</p>

                <div className="job-footer">
                  <button type="button" onClick={() => apply(job.jobId)} className="apply-btn">
                    <Send size={16} />
                    Apply
                  </button>
                  {job.applyLink && (
                    <a href={job.applyLink} target="_blank" rel="noreferrer" className="ghost-button">
                      Learn More
                    </a>
                  )}
                </div>
              </div>
            ))}
          </div>
        </SectionCard>
      )}
    </div>
  );
}
